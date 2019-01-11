package de.kramhal.callagent

import de.kramhal.callagent.services.SipgateServices
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource


internal class RestApiTest {

    @ParameterizedTest
    @CsvSource(
            "0873 376461, 0873376461",
            "03748 37682358, 0374837682358",
            "05444 347687-350, 05444347687350",
            "0764 812632-41, 076481263241",
            "0180 2 12334, 0180212334",
            "0800 5 23234213, 0800523234213",
            "+49 30 3432622-113, 0049303432622113",
            "+49 (355) 3432622-113, 00493553432622113",
            "+49(176)12345678, 004917612345678",
            "+49 (176) 1234 5678, 004917612345678",
            "+49 176 1234 5678, 004917612345678",
            "0049 176 1234 5678, 004917612345678",
            "0049 (355) 3432622-113, 00493553432622113",
            "0049(176)12345678, 004917612345678",
            "0049 (176) 1234 5678, 004917612345678",
            "0049 176 1234 5678, 004917612345678"
    )
    fun testExtractingNumbers(phoneIn: String, expected: String) {
        val testString = "und 127.0.0.1 wenn Tobi ein $phoneIn bisschen Text hat"
        val sipgateServices = SipgateServices(
                CallAgentPreferences.LoginData("", ""),
                CallAgentPreferences.SipgateUserDefaults("", "", ""))
        val actual = RestApi(sipgateServices).extractPhoneNumber(testString)
        assertEquals(expected, actual)
    }


}