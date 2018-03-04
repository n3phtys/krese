package krese

import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import krese.impl.*


fun main(args: Array<String>) {
    createInMemoryElements()
    Server(kodein).start()
}
