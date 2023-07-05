package de.oschwald.mofilegpttranslator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@SpringBootApplication
class MoFileGptTranslatorApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<MoFileGptTranslatorApplication>(*args)
        }
    }
}


