package krese


val configParentDir = "config"

fun main(args: Array<String>) {
    println("Hello World from JVM Server")

    //runExposedExample()

    createConfigFileDefaultIfNoConfigExists(configParentDir)

    executeWheneverConfigFileChanges( configParentDir) { config : Config ->
        println("Executing function for config ${config.toString()}")
    }

}
