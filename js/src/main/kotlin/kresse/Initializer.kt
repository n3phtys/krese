package kresse

import kotlin.browser.*
import kotlinx.html.*
import kotlinx.html.dom.*

fun main(args: Array<String>) {
    println("Hello World from JS Client")

    val root = document.getElementById("app")

    println(root)
}