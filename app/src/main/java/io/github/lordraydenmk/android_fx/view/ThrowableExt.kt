package io.github.lordraydenmk.android_fx.view

fun Throwable.errorMessage(): String =
    """${message ?: "Something went wrong :("}
        |
        |Tap here to retry!
    """.trimMargin()