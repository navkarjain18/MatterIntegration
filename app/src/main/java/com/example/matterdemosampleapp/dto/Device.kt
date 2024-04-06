package com.example.matterdemosampleapp.dto

data class MatterDevice(
     var vendorId: Int? = null,
     var productId: Int? = null,
     var name: String? = null,
     var room: String? = null,
     var deviceId: String? = null
)