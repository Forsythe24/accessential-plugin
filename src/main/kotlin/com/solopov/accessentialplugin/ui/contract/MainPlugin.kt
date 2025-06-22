package com.solopov.accessentialplugin.ui.contract

interface MainPlugin {
    fun build(postConstruction: (content: Any) -> Unit)
}