package com.decodex.bannapod

import com.decodex.bannapod.constants.Companion.CONTENT_TYPE
import com.decodex.bannapod.constants.Companion.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface Notification_API {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")

    suspend fun post_notification(
        @Body notification:Push_notification
    ): Response<ResponseBody>
}