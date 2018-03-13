package kresse

import kotlin.browser.*
import kotlinx.html.*
import kotlinx.html.dom.*
import krese.ClientState

val state = ClientState()

fun main(args: Array<String>) {
    println("Hello World from JS Client")
}


fun printSomethingByKotlin(something: String) {
    println(something)
}
