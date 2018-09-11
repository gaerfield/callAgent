package de.kramhal.callagent

import de.kramhal.callagent.services.SipgateServices
import mu.KLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
open class RestApi(
    private val sipgate : SipgateServices
) {
    companion object: KLogging()

    @GetMapping("/call/{phone}", produces = arrayOf("text/plain"))
    fun getBook(@PathVariable phone: String) : String{
        var normalized = phone.replace(" ", "").replace("(","").replace(")","")
        logger.info { "normalized [$phone] to [$normalized]" }
        if(!normalized.matches(Regex("""\+?[0-9]*""")))
            return "Invalid format: $phone"

        try {
            logger.info { "calling: $normalized" }
            return sipgate.call(normalized).sessionId
        } catch (e: Exception) {
            logger.error(e, { "request failed: ${e.message}" })
            return "request failed: ${e.message}"
        }
    }
}