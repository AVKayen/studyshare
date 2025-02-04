package com.studyshare.forms

enum class HtmxRequestType(val requestType: String) {
    GET("hx-get"),
    POST("hx-post"),
    PUT("hx-put"),
    PATCH("hx-patch"),
    DELETE("hx-delete")
}