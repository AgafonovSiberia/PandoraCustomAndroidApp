
package com.pandorawear.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.pandorawear.wear.phone.PhoneGateway
import com.pandorawear.wear.phone.WearPhoneGateway
import com.pandorawear.wear.presentation.PandoraWatchScreen
import com.pandorawear.wear.presentation.PandoraWatchViewModel
import com.pandorawear.wear.theme.PandoraWearTheme

class MainWear : ComponentActivity() {

    private val phoneGateway by lazy {
        WearPhoneGateway(applicationContext)
    }

    private val viewModel by viewModels<PandoraWatchViewModel> {
        PandoraWatchViewModelFactory(phoneGateway)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PandoraWearTheme {
                PandoraWatchScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.start()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stop()
    }
}

class PandoraWatchViewModelFactory(
    private val phoneGateway: PhoneGateway,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PandoraWatchViewModel::class.java)) {
            return PandoraWatchViewModel(phoneGateway) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}