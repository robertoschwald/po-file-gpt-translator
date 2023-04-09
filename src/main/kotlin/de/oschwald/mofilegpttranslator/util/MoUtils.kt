package de.oschwald.mofilegpttranslator.util


import org.fedorahosted.tennera.jgettext.Message
import org.fedorahosted.tennera.jgettext.PoParser
import java.io.File


/**
 * TODO: Add Docmentation
 * User: roos
 * Date: 31.03.23
 * Time: 09:33
 */
class MoUtils {
  companion object {
    fun readMoFile(moFile: File): List<org.fedorahosted.tennera.jgettext.Message> {
      val messageList = ArrayList<Message>()
      val poParser = PoParser()
      val catalog = poParser.parseCatalog(moFile)
      messageList.addAll(catalog.toList() as List<Message>)
      return messageList
    }
  }

}
