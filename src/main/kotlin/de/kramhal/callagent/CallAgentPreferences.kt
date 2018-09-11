package de.kramhal.callagent

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.prefs.Preferences
import kotlin.properties.Delegates
import kotlin.reflect.jvm.jvmName

@Configuration
open class CallAgentPreferences {
    private enum class PreferenceKeys { LoginData, SipgateUserDefaults }

    private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    private val prefs = Preferences.userRoot().node(CallAgentPreferences::class.jvmName)

    @JsonClass(generateAdapter = true)
    class LoginData(username : String = "", password: String = "") {
        @Transient var onChange = {  } /*byDefault nothing*/

        var username : String by Delegates.observable(username) { _, _, _ -> onChange() }
        var password: String by Delegates.observable(password) { _, _, _ -> onChange() }

        override fun toString(): String {
            val maskedPassword = "*".repeat(password.length)
            return "${LoginData::class}[username=$username, password=$maskedPassword]"
        }
    }
    private val loginDataAdapter = moshi.adapter(LoginData::class.java)

    @Bean
    open fun loadLoginData() : LoginData {
        val loginDataJson = prefs.get( PreferenceKeys.LoginData.name, loginDataAdapter.toJson(LoginData()))
        val loginData = loginDataAdapter.fromJson(loginDataJson) ?: LoginData()
        loginData.onChange = { saveLoginData(loginData) }
        return loginData
    }
    private fun saveLoginData(loginData: LoginData) =
        prefs.put("LoginData", loginDataAdapter.toJson(loginData))


    @JsonClass(generateAdapter = true)
    class SipgateUserDefaults(userId: String = "", deviceId: String = "", phone: String = "") {
        @Transient var onChange = {  } /*byDefault nothing*/

        var userId: String by Delegates.observable(userId) { _,_,_ -> onChange() }
        var deviceId: String by Delegates.observable(deviceId) { _,_,_ -> onChange() }
        var phone: String by Delegates.observable(phone) { _,_,_ -> onChange() }

        override fun toString(): String {
            return "${SipgateUserDefaults::class}[userId=$userId, deviceId=$deviceId, phone=$phone]"
        }
    }

    private val defaultsAdapter = moshi.adapter(SipgateUserDefaults::class.java)

    @Bean
    open fun sipgateUserDefaults() : SipgateUserDefaults {
        val userDefaultsJson = prefs.get(PreferenceKeys.SipgateUserDefaults.name, defaultsAdapter.toJson(SipgateUserDefaults()))
        val userDefaults = defaultsAdapter.fromJson(userDefaultsJson) ?: SipgateUserDefaults()
        userDefaults.onChange = { saveSipgateUserDefaults(userDefaults) }
        return userDefaults
    }
    private fun saveSipgateUserDefaults(userDefaults: SipgateUserDefaults) =
            prefs.put("SipgateUserDefaults", defaultsAdapter.toJson(userDefaults))

}