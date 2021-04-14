package com.decodex.bannapod

import java.io.Serializable


data class user(
    val firstname:String = "",
    val lastname:String = "",
    val age:Int = -1,
    val contact:String = "",
    val last_request_time:String = ""
) : Serializable
