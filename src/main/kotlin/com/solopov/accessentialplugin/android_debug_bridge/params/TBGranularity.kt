package com.solopov.accessentialplugin.android_debug_bridge.params

enum class TBGranularity {
    Default,
    Headings,
    Controls,
    Links,
    Words,
    Paragraphs,
    Characters,
    Lines,
    Window;

    val talkBackValue = this.name.lowercase()
}