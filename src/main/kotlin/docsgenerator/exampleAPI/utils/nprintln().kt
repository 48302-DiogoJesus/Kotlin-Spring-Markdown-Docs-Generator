package docsgenerator.exampleAPI.utils

fun nprintln(vararg data: Any?) {
    println(data.joinToString(" ") { it.toString() })
}
