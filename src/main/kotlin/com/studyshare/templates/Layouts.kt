package com.studyshare.templates

import com.studyshare.globalEnvironment
import kotlinx.html.*

fun HEAD.headTags() {
    val googleCloudStaticLocation = "https://storage.googleapis.com/${globalEnvironment!!.staticBucketName}"

    // htmx
    script { src = "$googleCloudStaticLocation/htmx-2.0.4.min.js" }
    script { src = "$googleCloudStaticLocation/hyperscript-0.9.3.min.js" }

    // pico.css
    link(rel = "stylesheet", href = "$googleCloudStaticLocation/pico-2.0.6.min.css")

    // Material Symbols Rounded
    link(rel = "stylesheet", href = "https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded")

    // fancybox
    script { src = "$googleCloudStaticLocation/fancybox-5.0.umd.js" }
    link(rel = "stylesheet", href = "$googleCloudStaticLocation/fancybox-5.0.css")

    // On development server, custom/private static files are served from /static
    val staticLocation: String = if (!globalEnvironment!!.production) "/static" else googleCloudStaticLocation
    link(rel = "stylesheet", href = "${staticLocation}/styles.css")
    script { src = "${staticLocation}/helperFunctions.js" }
    link(rel = "apple-touch-icon", href = "${staticLocation}/apple-touch-icon.png")
    link(rel = "icon", href = "${staticLocation}/favicon.ico")
    link(rel = "manifest", href = "${staticLocation}/site.webmanifest")


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
        headTags()
        title { +title }
    }
    body {
        attributes["_"] = """      
            on keydown
                set topModal to last <dialog/> in me
                if event.code == "Escape" and topModal
                    send closeModal to topModal
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