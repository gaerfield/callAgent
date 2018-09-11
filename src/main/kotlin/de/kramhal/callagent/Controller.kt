package de.kramhal.callagent

import de.kramhal.callagent.services.SipgateServices
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes


@Controller
class Controller(
        private val loginData : CallAgentPreferences.LoginData,
        private val userDefaults : CallAgentPreferences.SipgateUserDefaults,
        private val sipgate : SipgateServices
) {
    companion object: KLogging()

    @GetMapping("/")
    fun home(model: Model, redirectAttributes : RedirectAttributes): String {

        if(loginData.username.isBlank()) {
            messageError("Account-Settings not configured yet. Please fill in your Account-Details.", redirectAttributes)
            return "redirect:/account"
        }
        if(userDefaults.userId.isBlank()) {
            messageError("User-Preferences not configured yet. Please fill in your User-Preferences.", redirectAttributes)
            return "redirect:/preferences"
        }
        logger.info { "Current AccountSettings $loginData" }

        model.addAttribute("smsMessage", SmsMessage())
        model.addAttribute("phonecall", PhoneCall())
        return "index"
    }

    data class PhoneCall(val targetPhone: String = "", val sourcePhone: String = "", val fakeNumber: String = "")
    data class SmsMessage(val phone: String = "", val message: String = "")

    @PostMapping("/sendSms")
    fun sendSms(@ModelAttribute message: SmsMessage,
            redirectAttributes : RedirectAttributes) : String {
        executeAndReport("sms", { sipgate.sms(message.message, message.phone) }, redirectAttributes)
        return "redirect:/"
    }

    @PostMapping("/phonecall")
    fun phonecall(@ModelAttribute phonecall: PhoneCall,
                redirectAttributes : RedirectAttributes) : String {

        val source = if(phonecall.sourcePhone.isBlank()) userDefaults.phone else phonecall.sourcePhone
        val fake = if(phonecall.targetPhone.isBlank()) source else phonecall.fakeNumber

        executeAndReport("phonecall", { sipgate.call(phonecall.targetPhone, source, fake) }, redirectAttributes)
        return "redirect:/"
    }

    @GetMapping("/testApiReachability")
    fun testApiReachability(redirectAttributes : RedirectAttributes): String {
        executeAndReport("ping", { sipgate.ping() }, redirectAttributes)
        return "redirect:/"
    }

    @GetMapping("/testAuthentication")
    fun testAuthentication(redirectAttributes : RedirectAttributes): String {
        executeAndReport("authenticatedPing", { sipgate.users() }, redirectAttributes)
        return "redirect:/";
    }

    @GetMapping("/account")
    fun account(model: Model): String {
        model.addAttribute("loginData", loginData);
        return "account"
    }

    @PostMapping("/account")
    fun account(@ModelAttribute loginData: CallAgentPreferences.LoginData): String {
        logger.info { "AccountSettings changed: $loginData" }
        with(this.loginData){
            username = loginData.username
            password = loginData.password
        }
        return "redirect:/account"
    }

    data class UserAndDevice(val userId : String, val firstName : String, val lastName : String,
                             val deviceId : String, val deviceAlias : String, val default : Boolean) {
        constructor(user: SipgateServices.User, device: SipgateServices.Device) : this(
            user.id, user.firstname, user.lastname,
            device.id, device.alias, device.id == user.defaultDevice
        )

        fun fullName() = "$firstName $lastName"
    }

    @GetMapping("/preferences")
    fun preferences(model: Model, redirectAttributes : RedirectAttributes): String {
        if(loginData.username.isBlank()) {
            messageError("You must configure Account-Settings first.", redirectAttributes)
            return "redirect:/account"
        }

        val usersAndDevices = sipgate.users()
                .flatMap { user ->
                    sipgate.devices(user.id)
                            .map { device -> UserAndDevice(user, device) }
                }

        if(userDefaults.userId.isBlank())
        {
            val firstDefault = usersAndDevices.firstOrNull { it.default } ?: usersAndDevices.firstOrNull()
            with(userDefaults) {
                userDefaults.userId = firstDefault?.userId ?: ""
                userDefaults.deviceId = firstDefault?.deviceId ?: ""
            }
        }

        model.addAttribute("userDefaults", userDefaults);
        model.addAttribute("usersAndDevices", usersAndDevices);
        return "preferences"
    }

    @PostMapping("/preferences")
    fun preferences(@ModelAttribute userDefaults: CallAgentPreferences.SipgateUserDefaults): String {
        logger.info { "UserDefaults changed: $userDefaults" }
        with(this.userDefaults){
            userId = userDefaults.userId
            deviceId = userDefaults.deviceId
            phone = userDefaults.phone
        }
        return "redirect:/preferences"
    }


    private fun executeAndReport(name: String, function: () -> Any, redirectAttributes : RedirectAttributes) {
        try {
            logger.info { name }
            function()
            logger.info { "$name was successful" }
            messageOk("$name was successful", redirectAttributes)

        } catch (e : Exception) {
            logger.error(e, { "Error during $name" })
            messageError("Error during $name: ${e.message}", redirectAttributes)
        }
    }

    private fun messageOk(message: String, redirectAttributes: RedirectAttributes) {
        redirectAttributes.addFlashAttribute("message", message)
        redirectAttributes.addFlashAttribute("alertClass", "alert-success")
    }
    private fun messageError(message: String, redirectAttributes: RedirectAttributes) {
        redirectAttributes.addFlashAttribute("message", message)
        redirectAttributes.addFlashAttribute("alertClass", "alert-danger")
    }

}