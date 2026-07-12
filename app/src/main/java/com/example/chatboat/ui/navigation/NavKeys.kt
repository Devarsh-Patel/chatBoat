package com.example.chatboat.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute : NavKey {
    @Serializable
    data object Auth : NavRoute

    @Serializable
    data object Main : NavRoute
}
