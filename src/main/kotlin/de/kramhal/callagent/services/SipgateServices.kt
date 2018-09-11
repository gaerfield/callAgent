package de.kramhal.callagent.services

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.kramhal.callagent.CallAgentPreferences
import de.kramhal.sipgateShell.sipgateApi.AccessTokenAuthInterceptor
import de.kramhal.sipgateShell.sipgateApi.UnitConverterFactory
import de.kramhal.sipgateShell.sipgateApi.failOnError
import mu.KotlinLogging
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.springframework.stereotype.Component
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

@Component
open class SipgateServices(
        private val loginData: CallAgentPreferences.LoginData,
        private val userDefaults: CallAgentPreferences.SipgateUserDefaults
) {
    companion object {
        val API_BASE_URL = "https://api.sipgate.com/v2/"
        val logger = KotlinLogging.logger {}
    }

    private val jsonConverter = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

    private fun <T> getRestApi(api : Class<T>, token : Token? = null,
                               logging : Boolean = false) : T {
        val httpClientBuilder = OkHttpClient.Builder()
        if(token != null)
            httpClientBuilder.addInterceptor(AccessTokenAuthInterceptor(token))
        if(logging) {
            val logger = HttpLoggingInterceptor()
            logger.level = HttpLoggingInterceptor.Level.BODY
            httpClientBuilder.addInterceptor(logger)
        }

        val builder = Retrofit.Builder()
                .client(httpClientBuilder.build())
                .baseUrl(API_BASE_URL)
                .addConverterFactory(UnitConverterFactory)
                .addConverterFactory(MoshiConverterFactory.create(jsonConverter))
                .build()

        return builder.create(api)
    }

    // Sipgate - Token - RestApi

    @JsonClass(generateAdapter = true)
    internal data class Token(val token : String)
    @JsonClass(generateAdapter = true)
    internal data class SipgateLoginData(val username : String, val password : String)
    internal interface SipgateTokenApi{
        @POST("authorization/token")
        fun aquireToken(@Body loginData: SipgateLoginData) : Call<Token>
    }
    private fun aquireToken() = failOnError {
        getRestApi(SipgateTokenApi::class.java)
                .aquireToken(SipgateLoginData(loginData.username, loginData.password))
                .execute()
    }

    // Sipgate-RestApi

    @JsonClass(generateAdapter = true)
    data class Pong(val ping : String)

    @JsonClass(generateAdapter = true)
    data class User(val id : String, val firstname: String, val lastname: String,
                    val email : String, val defaultDevice : String,
                    val busyOnBusy : Boolean, val admin : Boolean)
    @JsonClass(generateAdapter = true)
    internal data class Users(val items : List<User>)

    @JsonClass(generateAdapter = true)
    data class Sms(val smsId: String, val recipient: String, val message: String)
    @JsonClass(generateAdapter = true)
    data class SmsId(val id : String, val alias: String, val callerId: String)
    @JsonClass(generateAdapter = true)
    internal data class SmsIds(val items : List<SmsId>)

    @JsonClass(generateAdapter = true)
    data class PhoneCall(val deviceId: String = "", val caller: String, val callee: String, val callerId: String? = null)
    @JsonClass(generateAdapter = true)
    data class SessionId(val sessionId: String)

    @JsonClass(generateAdapter = true)
    data class Device(val id: String, val alias: String, val type: String, val online: Boolean, val dnd: Boolean)
    @JsonClass(generateAdapter = true)
    data class Devices(val items: List<Device>)

    private interface SipgateApi{
        @GET("ping")
        fun ping(): Call<Pong>
        @GET("users")
        fun users() : Call<Users>
        @GET("{userId}/sms")
        fun smsIds(@Path("userId") userId: String) : Call<SmsIds>
        @POST("sessions/sms")
        fun sms(@Body sms: Sms) : Call<Unit>
        @POST("sessions/calls")
        fun call(@Body call: PhoneCall) : Call<SessionId>
        @GET("{userId}/devices")
        fun devices(@Path("userId") userId: String) : Call<Devices>
    }

    private val sipgateApi by lazy { getRestApi(SipgateApi::class.java, aquireToken()) }

    // wrapped Services-Requests

    fun ping() = failOnError { getRestApi(SipgateApi::class.java).ping().execute() }
    fun users() = failOnError { sipgateApi.users().execute() }.items
    fun smsIds(userId: String) = failOnError { sipgateApi.smsIds(userId).execute() }.items
    fun sms(message : String, phone : String) = failOnError {
        val firstSmsId = smsIds(userDefaults.userId)[0].id
        val sms = Sms(firstSmsId, phone, message)
        logger.info { sms }
        sipgateApi.sms(sms).execute()
    }

    fun call(targetPhone: String, sourcePhone: String = userDefaults.phone, fakePhone: String = sourcePhone) = failOnError {
        val phoneCall = PhoneCall(deviceId = userDefaults.deviceId,
                caller = sourcePhone, callee = targetPhone, callerId = fakePhone)
        logger.info { phoneCall }
        sipgateApi.call(phoneCall).execute()
    }

    fun devices(userId: String) = failOnError { sipgateApi.devices(userId).execute() }.items


}