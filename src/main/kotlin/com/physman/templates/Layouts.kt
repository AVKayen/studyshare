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

fun BODY.layoutHeader(
    username: String? = null,
    breadcrumbs: Map<String, String>? = null,
    lastBreadcrumb: String? = null
) {
    header(classes = "container") {
        if (username != null) {
            nav {
                ul {
                    li { h1 { +"SchoolShare" } }
                }
                ul {
                    li {
                        details(classes = "dropdown") {
                            summary {
                                // user profile icon
                                unsafe {
                                    +"""
                                        <svg xmlns="http://www.w3.org/2000/svg" height="32" width="28" viewBox="0 0 448 512"> 
                                            <!--!Font Awesome Free 6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2025 Fonticons, Inc.-->
                                            <path d="M224 256A128 128 0 1 0 224 0a128 128 0 1 0 0 256zm-45.7 48C79.8 304 0 383.8 0 482.3C0 498.7 13.3 512 29.7 512l388.6 0c16.4 0 29.7-13.3 29.7-29.7C448 383.8 368.2 304 269.7 304l-91.4 0z"/>
                                        </svg>
                                    """.trimIndent()
                                }
                            }
                            ul {
                                attributes["dir"] = "rtl"
                                li { +"Logged in as $username" }
                                li { a(href = "/auth/logout") { +"Logout" } }
                            }
                        }
                    }
                }
            }
        }

        if (breadcrumbs != null && lastBreadcrumb != null) {
            nav {
                attributes["aria-label"] = "breadcrumb"

                ul {
                    breadcrumbs.forEach { (description, href) ->
                        li { a(href = href) { +description } }
                    }
                    li { +lastBreadcrumb }
                }
            }
        }
    }
}

// Main layout
fun HTML.index(
    title: String,
    username: String? = null,
    breadcrumbs: Map<String, String>? = null,
    lastBreadcrumb: String? = null,
    block : MAIN.() -> Unit
) {
    head {
        headTags()
        title { +title }
    }
    body {
        layoutHeader(username = username, breadcrumbs = breadcrumbs, lastBreadcrumb = lastBreadcrumb)
        main(classes = "container") {
            block()
        }
    }
}