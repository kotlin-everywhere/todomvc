package com.minek.kotlin.everywhere.todomvc

import com.minek.kotlin.everywhere.keuix.browser.html.Html
import com.minek.kotlin.everywhere.keuix.browser.runBeginnerProgram
import org.w3c.dom.Element

class Model
class Msg

fun update(msg: Msg, model: Model): Model {
    return model
}

fun view(model: Model): Html<Msg> {
    return Html.text("Hello, Todo MVC")
}

@Suppress("unused")
@JsName("main")
fun main(container: Element) {
    runBeginnerProgram(container, Model(), ::update, ::view)
}