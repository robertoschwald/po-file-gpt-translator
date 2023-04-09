package de.oschwald.mofilegpttranslator.controller

import de.oschwald.mofilegpttranslator.services.TranslationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.ModelAndView
import java.io.File

@Controller
class UploadController(private val translationService: TranslationService) {
  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  private val supportedLanguages = listOf("English", "Spanish", "French", "German", "German Formal", "Chinese", "Japanese")

  @GetMapping("/upload")
  fun showUploadForm(model: Model): ModelAndView {
    model.addAttribute("supportedLanguages", supportedLanguages)
    return ModelAndView("upload")
  }

  @PostMapping("/upload")
  fun translateFile(
    @RequestParam("uploadFile") mpFile: MultipartFile,
    @RequestParam("targetLanguage") targetLanguage: String,
    model: Model
  ): ModelAndView {
    val file = File.createTempFile(mpFile.name, "tmp")
    mpFile.transferTo(file)
    val translatedContent = translationService.translate(file, targetLanguage)


    return if (translatedContent.isNotEmpty()) {
      model.addAttribute("message", "File uploaded and translated successfully.")
      model.addAttribute("translatedContent", translatedContent)
      ModelAndView("upload", model.asMap())
    } else {
      model.addAttribute("error", "Translation failed.")
      ModelAndView("upload", model.asMap())
    }
  }
}