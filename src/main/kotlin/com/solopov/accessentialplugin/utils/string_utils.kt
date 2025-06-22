package com.solopov.accessentialplugin.utils

import org.jetbrains.annotations.NonNls
import java.util.*

@NonNls
private val bundle = ResourceBundle.getBundle("Strings")

fun getString(stringName: String, vararg params: Any?): String {
    val value = bundle.getString(stringName)

    return value.format(*params)
}