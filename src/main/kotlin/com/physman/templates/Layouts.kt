package com.physman.templates

import kotlinx.html.*

fun HEAD.headTags() {
    // htmx
    script { src = "https://unpkg.com/htmx.org@2.0.4" }
    // picoCSS defaults
    link(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css")
    // custom CSS
    link(rel = "stylesheet", href = "/static/styles.css")
}

// Main layout
fun HTML.index(title: String, block : BODY.() -> Unit) {
    head {
        headTags()
        title { +title }
    }
    body {
        block()
    }
}