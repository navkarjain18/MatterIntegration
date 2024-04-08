package com.example.matterdemosampleapp.utils

import com.google.gson.Gson

fun Any.encode(mapper: Class<*>): String {
    return Gson().toJson(this, mapper)
}