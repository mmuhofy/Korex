package com.korexx.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.korexx.terminal.BootstrapInstaller
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BootstrapState {
    data object Checking : BootstrapState
    data class Installing(val message: String, val percent: Int) : BootstrapState
    data object Done : BootstrapState
    data class Error(val message: String) : BootstrapState
}

@HiltViewModel
class BootstrapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow<BootstrapState>(BootstrapState.Checking)
    val state: StateFlow<BootstrapState> = _state.asStateFlow()

    init {
        checkAndInstall()
    }

    private fun checkAndInstall() {
        viewModelScope.launch {
            if (BootstrapInstaller.isInstalled(context)) {
                _state.value = BootstrapState.Done
                return@launch
            }

            if (!BootstrapInstaller.isLoaded) {
                _state.value = BootstrapState.Error(
                    "Bootstrap library not found in APK. Please reinstall Korex."
                )
                return@launch
            }

            try {
                BootstrapInstaller.install(context) { message, percent ->
                    _state.value = BootstrapState.Installing(message, percent)
                }
                _state.value = BootstrapState.Done
            } catch (e: Exception) {
                _state.value = BootstrapState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun retry() {
        _state.value = BootstrapState.Checking
        checkAndInstall()
    }
}