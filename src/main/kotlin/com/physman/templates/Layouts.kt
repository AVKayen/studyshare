package com.physman.templates

import kotlinx.html.*

fun HEAD.headTags() {
    // htmx
    script { src = "https://unpkg.com/htmx.org@2.0.4" }
    // _hyperscript
    script { src = "https://unpkg.com/hyperscript.org@0.9.13"}
    // picoCSS defaults
    link(rel = "stylesheet", href = "https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css")
    // material icons
    link(rel = "stylesheet", href = "https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded")
    // custom CSS TODO: for prod, change this to a GCP file
    // link(rel = "stylesheet", href = "https://storage.googleapis.com/studyshare-static/styles.css")
    link(rel = "stylesheet", href = "/static/styles.css")
    
    // config for htmx
    meta(name = "htmx-config", content = """
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

                    li {
                        if (breadcrumbs != null && lastBreadcrumb != null) {
                            nav {
                                attributes["aria-label"] = "breadcrumb"

                                a {
                                    href = "/"
                                    classes = setOf("nav-title")
                                    +"SchoolShare"
                                }
                                ul {
                                    classes = setOf("breadcrumb")
                                    li {}
                                    breadcrumbs.forEach { (description, href) ->
                                        li { a(href = href) { +description } }
                                    }
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