package com.example.matterdemosampleapp.utils

import com.example.matterdemosampleapp.DeviceType
import java.security.SecureRandom
import kotlin.math.abs

object AppUtils {
    fun convertToAppDeviceType(matterDeviceType: Long): DeviceType {
        return when (matterDeviceType) {
            256L -> DeviceType.TYPE_LIGHT // 0x0100 On/Off Light
            257L -> DeviceType.TYPE_DIMMABLE_LIGHT // 0x0101 Dimmable Light
            259L -> DeviceType.TYPE_LIGHT_SWITCH // 0x0103 On/Off Light Switch
            266L -> DeviceType.TYPE_OUTLET // 0x010A (On/Off Plug-in Unit)
            268L -> DeviceType.TYPE_COLOR_TEMPERATURE_LIGHT // 0x010C Color Temperature Light
            269L -> DeviceType.TYPE_EXTENDED_COLOR_LIGHT // 0x010D Extended Color Light
            else -> DeviceType.TYPE_UNKNOWN
        }
    }

    fun stripLinkLocalInIpAddress(ipAddress: String): String {
        return ipAddress.replace("%.*".toRegex(), "")
    }

    /** Generates a random number to be used as a device identifier during device commissioning */
    fun generateNextDeviceId(): Long {
        val secureRandom =
            try {
                SecureRandom.getInstance("SHA1PRNG")
            } catch (ex: Exception) {
                // instantiate with the default algorithm
                SecureRandom()
            }

        return java.lang.Long.max(abs(secureRandom.nextLong()), 1)
    }
}