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

    val phoneNumberRegex = """([0-9()/+\s\-]{5,})""".toRegex()
    val areaCodeRegex = """(\+[0-9]{2}[0-9]+)""".toRegex()
    val charactersToBeReplaced = """[-\s()]""".toRegex()

    @GetMapping("/call/{phone}", produces = arrayOf("text/plain"))
    fun call(@PathVariable phone: String): String {
        val normalized = extractPhoneNumber(phone)
        logger.info { "normalized [$phone] to [$normalized]" }
        if (normalized == null)
            return "Invalid format: $phone"

        try {
            logger.info { "calling: $normalized" }
            val call = sipgate.call(normalized)
            return "I called $normalized <br /> This is the session-Id: ${call.sessionId}"
        } catch (e: Exception) {
            logger.error(e, { "request failed: ${e.message}" })
            return "request failed: ${e.message}"
        }
    }

    fun extractPhoneNumber(value: String): String? {
        var match = phoneNumberRegex.find(value)?.value
        match = match?.replace(charactersToBeReplaced, "")
        if (match != null && areaCodeRegex.matches(match))
            match = match.replace("+", "00")
        return match
    }
}