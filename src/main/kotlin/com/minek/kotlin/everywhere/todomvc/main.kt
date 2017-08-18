package com.minek.kotlin.everywhere.todomvc

import com.minek.kotlin.everywhere.keuix.browser.html.Html
import com.minek.kotlin.everywhere.keuix.browser.runBeginnerProgram
import org.w3c.dom.Element

@Suppress("unused")
@JsName("main")
fun main(container: Element) {
    runBeginnerProgram(container, Html.text("Hello, Todo MVC"))
}