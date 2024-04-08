package com.example.matterdemosampleapp.dto

import com.example.matterdemosampleapp.DeviceType
import java.io.Serializable


data class MatterDevices(
     var matterDeviceList : List<MatterDevice>
): Serializable

data class MatterDevice(
     var vendorId: Int? = null,
     var productId: Int? = null,
     var name: String? = null,
     var room: String? = null,
     var deviceId: Long? = null,
     var deviceType: DeviceType? = null,
     var isOn : Boolean = false
): Serializable