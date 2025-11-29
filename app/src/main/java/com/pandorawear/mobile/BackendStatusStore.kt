package com.pandorawear.mobile

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object BackendStatusStore {

    private val _state = MutableStateFlow(AppState.BACKEND_UNAVAILABLE)
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun set(state: AppState) {
        _state.value = state
    }

    fun current(): AppState = _state.value
}