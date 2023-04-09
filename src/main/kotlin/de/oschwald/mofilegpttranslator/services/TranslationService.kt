package de.oschwald.mofilegpttranslator.services

import de.oschwald.mofilegpttranslator.util.MoUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.fedorahosted.tennera.jgettext.Catalog
import org.fedorahosted.tennera.jgettext.Message
import org.fedorahosted.tennera.jgettext.PoWriter
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.Properties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.util.*

@Service
class TranslationService {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  private val properties = loadExternalProperties()
  private val apiKey = properties.getProperty("openai.api.key")
  private val endpointUrl = properties.getProperty("openai.api.endpoint") ?: "https://api.openai.com/v1/chat/completions"

  private fun loadExternalProperties(): Properties {
    val homeDirectory = System.getProperty("user.home")
    val propertiesFilePath = Paths.get(homeDirectory, ".chatgpt.properties").toString()
    val properties = Properties()
    FileInputStream(propertiesFilePath).use { inputStream ->
      properties.load(inputStream)
    }
    return properties
  }

  fun translate(moFile: File, targetLanguage: String): String {
    val messages = MoUtils.readMoFile(moFile)

    // Find untranslated messages
    val untranslatedMessages = messages.filter {
      it.msgid != null && it.msgstr.isNullOrBlank() || it.msgid != it.msgstr
    }

    val untranslatedKeys = untranslatedMessages.map { it.msgid }
    // Translate messages using GPT
    val translatedMessages = messages.map { message ->
      if (untranslatedKeys.contains(message.msgid)) {
        val translatedText = translateText(message.msgid, targetLanguage)
        message.msgstr = translatedText
        message
      } else {
        message
      }
    }

    // Generate new .mo file with translations
    return generateMoFile(translatedMessages)
  }

  private fun translateText(text: String, targetLanguage: String): String {
    if (text.isBlank()) return ""
    val httpClient = OkHttpClient()
      .newBuilder()
//      .addInterceptor(HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BASIC
//      })
      .build()
    //
    val jsonObject = JSONObject()
    val chatText = "Translate the following text to $targetLanguage: \"$text\""
    jsonObject.put("model", "gpt-3.5-turbo") // Switch to GPT-4 when it's available
    jsonObject.put("messages", listOf(mapOf("role" to "user", "content" to chatText)))
    //
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = RequestBody.create(mediaType, jsonObject.toString())
    val request = Request.Builder()
      .url(endpointUrl)
      .header("Content-Type", "application/json")
      .header("Authorization", "Bearer $apiKey")
      .post(requestBody)
      .build()
    val response = httpClient.newCall(request).execute()
    val jsonResponse = JSONObject(response.body!!.string())
    val translatedText = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
    log.info("Translated text {} to: {}", text, translatedText)
    return translatedText
  }

  private fun generateMoFile(messages: List<Message>): String {
    val outputStream = ByteArrayOutputStream()
    val writer = OutputStreamWriter(outputStream, Charset.forName("UTF-8"))
    val catalog = Catalog()
    messages.forEach { message ->
      catalog.addMessage(message)
    }
    PoWriter().write(catalog, writer)
    return outputStream.toString(Charset.forName("UTF-8"))
  }

}

