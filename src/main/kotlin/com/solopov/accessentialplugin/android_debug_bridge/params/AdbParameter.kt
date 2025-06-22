package com.solopov.accessentialplugin.android_debug_bridge.params

interface AdbParameter {
    fun toAdbArgument(): String
}

class EmptyAdbParameter: AdbParameter {
    override fun toAdbArgument(): String = Unit.toString()
}

enum class BooleanOutputFormat{
    NUMERIC, TRUE_FALSE, YES_NO, DECIMAL
}

class BooleanAdbParameter(private val value: Boolean, private val format: BooleanOutputFormat = BooleanOutputFormat.NUMERIC): AdbParameter {
    override fun toAdbArgument(): String =
        when(format) {
            BooleanOutputFormat.NUMERIC -> if (value) "1" else "0"
            BooleanOutputFormat.TRUE_FALSE -> value.toString()
            BooleanOutputFormat.YES_NO -> if (value) "yes" else "no"
            BooleanOutputFormat.DECIMAL -> if (value) "1.0" else "0.0"
        }
}

class GenericAdbParameter<T>(private val value: T, private val forceLowerCase: Boolean = false): AdbParameter {
    override fun toAdbArgument(): String = if (forceLowerCase) value.toString().lowercase() else value.toString()
}

class TextInputAdbParameter(private val content: String): AdbParameter {
    override fun toAdbArgument(): String = "\"${ content.replace(" ", "\\ ") }\""
}

class ColorAdjustmentAdbParameter(private val adjustment: ColorAdjustmentMode): AdbParameter {
    override fun toAdbArgument(): String = adjustment.adbParameterValue.toString()
}

class NonNegativeAdbParameter(private val data: Int, private val inNegativeCase: String): AdbParameter {
    override fun toAdbArgument(): String = if (data < 0) inNegativeCase else "\"${data}\""
}