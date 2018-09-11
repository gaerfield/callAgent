package de.kramhal.sipgateShell.sipgateApi

import de.kramhal.callagent.CallAgentPreferences
import de.kramhal.callagent.services.SipgateServices
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type


/** Thrown, if an api-call fails */
class RequestFailedException(message : String) : Exception(message)

fun <T> failOnError(function: () -> retrofit2.Response<T>) : T {
    val response = function.invoke()

    if(response.isSuccessful)
        when(response.body()) {
            null -> return Unit as T
            else -> return response.body()!!
        }


    val errorMessage = "Failed: ${response.code()}: ${response.message()}\n${response.errorBody()?.string()}"
    throw RequestFailedException(errorMessage)
}

internal class AccessTokenAuthInterceptor(private val token: SipgateServices.Token) : Interceptor {
    override fun intercept(chain: Interceptor.Chain?): Response {
        val request = chain!!.request().newBuilder()
                .header("Authorization", "Bearer ${token.token}")
                .build();
        return chain.proceed(request)
    }
}

class BasicAuthInterceptor(private val loginData: CallAgentPreferences.LoginData) : Interceptor {
    override fun intercept(chain: Interceptor.Chain?): Response {
        val request = chain!!.request().newBuilder()
                .header("Authorization", Credentials.basic(loginData.username, loginData.password))
                .build();
        return chain.proceed(request)
    }
}

object UnitConverterFactory : Converter.Factory() {
    override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>,
                                       retrofit: Retrofit): Converter<ResponseBody, *>? {
        return if (type == Unit::class.java) UnitConverter else null
    }

    private object UnitConverter : Converter<ResponseBody, Unit> {
        override fun convert(value: ResponseBody) {
            value.close()
        }
    }
}