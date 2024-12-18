package com.example

import kotlinx.html.*

// Main layout
fun HTML.index(title: String, block : BODY.() -> Unit){
    head {
        title { +title }
        link(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css")
        link(rel = "stylesheet", href = "static/styles.css")
    }
    body {
        block()
    }
}