package com.decodex.bannapod

//class for sending notification
data class Push_notification(
    val data:Notification_data,
    val to:String //Pass topic here or Token to send
)
