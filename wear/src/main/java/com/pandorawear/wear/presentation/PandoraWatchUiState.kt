package com.pandorawear.wear.presentation

import com.pandorawear.wear.models.WatchPandoraStatus

sealed interface PandoraWatchUiState {

    data object Loading : PandoraWatchUiState

    data class NotReady(
        val lastKnownStatus: WatchPandoraStatus? = null,
        val message: String? = null,
    ) : PandoraWatchUiState

    data class Ready(
        val status: WatchPandoraStatus,
    ) : PandoraWatchUiState


    data class Error(
        val message: String,
        val lastKnownStatus: WatchPandoraStatus? = null,
    ) : PandoraWatchUiState
}