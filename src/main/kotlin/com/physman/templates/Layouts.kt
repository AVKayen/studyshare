package com.physman.templates

import com.physman.isDevelopment
import kotlinx.html.*

fun HEAD.headTags(isDevelopment: Boolean = false) {
    // htmx
    script { src = "https://storage.googleapis.com/studyshare-static/htmx-2.0.4.min.js" }
    // _hyperscript
    script { src = "https://storage.googleapis.com/studyshare-static/hyperscript-0.9.3.min.js" }
    // picoCSS defaults
    link(rel = "stylesheet", href = "https://storage.googleapis.com/studyshare-static/pico-2.0.6.min.css")
    // material icons
    link(rel = "stylesheet", href = "https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded")

    // fancybox
    script { src = "https://storage.googleapis.com/studyshare-static/fancybox-5.0.umd.js" }
    link(rel = "stylesheet", href = "https://storage.googleapis.com/studyshare-static/fancybox-5.0.css")

    val staticLocation: String = if (isDevelopment) "/static" else "https://storage.googleapis.com/studyshare-static"
    // custom CSS and JS
    link(rel = "stylesheet", href = "${staticLocation}/styles.css")
    script { src = "${staticLocation}/helperFunctions.js" }

    // config for htmx (code 422 for error form reponses)
    meta(
        name = "htmx-config", content = """
        {
            "responseHandling":[
                {"code":"204", "swap": false},
                {"code":"[23]..", "swap": true},
                {"code":"422", "swap": true, "error": true},
                {"code":"[45]..", "swap": false, "error":true},
                {"code":"...", "swap": true}
            ]
        }
    """.trimIndent()
    )
    meta {
        name = "viewport"
        content = "width=device-width, initial-scale=1"
    }
}

fun BODY.layoutHeader(
    username: String? = null,
    breadcrumbs: Map<String, String>? = null,
    lastBreadcrumb: String? = null
) {
    header(classes = "container") {
        nav {
            ul {
                li {
                    nav {
                        attributes["aria-label"] = "breadcrumb"
                        a {
                            href = "/"
                            classes = setOf("nav-title")
                            +"StudyShare"
                        }
                        ul {
                            classes = setOf("breadcrumb")
                            li {} // Needed to create a leading '>' sign
                            if (!breadcrumbs.isNullOrEmpty()) {
                                breadcrumbs.forEach { (description, href) ->
                                    li { a(href = href) { +description } }
                                }
                            }
                            if (lastBreadcrumb != null) {
                                li { +lastBreadcrumb }
                            }
                        }
                    }
                }
            }
            ul {
                li {
                    details(classes = "dropdown") {
                        summary {
                            span {
                                classes = setOf("material-symbols-rounded", "dropdown-icon")
                                +"account_circle"
                            }
                        }
                        if (username != null) {
                            ul {
                                attributes["dir"] = "rtl"
                                li {
                                    classes = setOf("dropdown-login-info")
                                    +"Logged in as $username"
                                }
                                li {
                                    classes = setOf("dropdown-logout")
                                    a(href = "/auth/logout") {
                                        +"Logout"
                                    }
                                }
                            }
                        } else {
                            ul {
                                attributes["dir"] = "rtl"
                                li {
                                    a(href = "/auth/login") {
                                        +"Login"
                                    }
                                }
                                li {
                                    a(href = "/auth/register") {
                                        +"Register"
                                    }
                                }
                            }
                        }
                    }
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
    block: MAIN.() -> Unit
) {
    head {
        headTags(isDevelopment)
        title { +title }
    }
    body {
        attributes["_"] = """      
            on keydown
                if event.code == "Escape"
                    send closeModal to .modal
                end
            end
        """.trimIndent()
        layoutHeader(username = username, breadcrumbs = breadcrumbs, lastBreadcrumb = lastBreadcrumb)
        main(classes = "container") {
            block()
        }
        fancyboxSetupScript()
    }
}