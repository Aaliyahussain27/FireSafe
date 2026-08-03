package com.example.smartflame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartflame.data.repository.*
import com.example.smartflame.data.repository.impl.*
import com.example.smartflame.data.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmergencyViewModel(
    private val locationRepository: LocationRepository = MockLocationRepository(),
    private val fireStationRepository: FireStationRepository = MockFireStationRepository(),
    private val alertRepository: AlertRepository = MockAlertRepository(),
    private val otpRepository: OtpRepository = MockOtpRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<EmergencyFlowState>(EmergencyFlowState.Idle)
    val uiState: StateFlow<EmergencyFlowState> = _uiState.asStateFlow()

    private var otpTimerJob: Job? = null

    fun startEmergency() {
        _uiState.value = EmergencyFlowState.DetectingLocation
    }

    fun resolveLocationAndDispatch(onResolved: () -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Get GPS Location
                val location = locationRepository.getCurrentLocation()
                
                // 2. Dispatch Alert to Central Hub
                val alertConfirm = alertRepository.dispatchAlert(
                    location.latitude,
                    location.longitude,
                    location.address
                )
                
                // 3. Find Nearest Fire Station
                val station = fireStationRepository.getNearestStation(
                    location.latitude,
                    location.longitude
                )
                
                _uiState.value = EmergencyFlowState.AlertSent(
                    location = location,
                    alertConfirmation = alertConfirm,
                    fireStation = station
                )
                onResolved()
            } catch (e: Exception) {
                // If anything fails, revert to Idle or handle error. For now revert to Idle.
                _uiState.value = EmergencyFlowState.Idle
            }
        }
    }

    fun showCancelConfirmationDialog(show: Boolean) {
        _uiState.update { currentState ->
            if (currentState is EmergencyFlowState.AlertSent) {
                currentState.copy(isCancelling = show)
            } else {
                currentState
            }
        }
    }

    fun cancelAlert(onCancelled: () -> Unit) {
        val currentState = _uiState.value
        if (currentState is EmergencyFlowState.AlertSent) {
            viewModelScope.launch {
                alertRepository.cancelAlert(currentState.alertConfirmation.alertId)
                _uiState.value = EmergencyFlowState.Idle
                onCancelled()
            }
        }
    }

    fun proceedToPhoto() {
        val currentState = _uiState.value
        if (currentState is EmergencyFlowState.AlertSent) {
            _uiState.value = EmergencyFlowState.AwaitingPhoto(
                location = currentState.location,
                alertConfirmation = currentState.alertConfirmation,
                fireStation = currentState.fireStation
            )
        }
    }

    fun savePhotoAndProceed(photoPath: String?, defaultPhoneNum: String = "") {
        val currentState = _uiState.value
        val (location, alertConfirm, station) = when (currentState) {
            is EmergencyFlowState.AwaitingPhoto -> Triple(currentState.location, currentState.alertConfirmation, currentState.fireStation)
            is EmergencyFlowState.AlertSent -> Triple(currentState.location, currentState.alertConfirmation, currentState.fireStation)
            else -> return
        }

        _uiState.value = EmergencyFlowState.AwaitingOTP(
            location = location,
            alertConfirmation = alertConfirm,
            fireStation = station,
            photoPath = photoPath,
            phoneNumber = defaultPhoneNum,
            otpError = null,
            isVerifying = false,
            countdownSeconds = 30
        )
        startOtpCountdown()
    }

    fun startOtpCountdown() {
        otpTimerJob?.cancel()
        otpTimerJob = viewModelScope.launch {
            while (true) {
                val state = _uiState.value
                if (state is EmergencyFlowState.AwaitingOTP) {
                    if (state.countdownSeconds > 0) {
                        delay(1000)
                        _uiState.update { current ->
                            if (current is EmergencyFlowState.AwaitingOTP) {
                                current.copy(countdownSeconds = current.countdownSeconds - 1)
                            } else {
                                current
                            }
                        }
                    } else {
                        break
                    }
                } else {
                    break
                }
            }
        }
    }

    fun resendOtp() {
        val state = _uiState.value
        if (state is EmergencyFlowState.AwaitingOTP) {
            viewModelScope.launch {
                otpRepository.sendOtp(state.phoneNumber)
                _uiState.update { current ->
                    if (current is EmergencyFlowState.AwaitingOTP) {
                        current.copy(countdownSeconds = 30, otpError = null)
                    } else {
                        current
                    }
                }
                startOtpCountdown()
            }
        }
    }

    fun updatePhoneNumber(newNumber: String) {
        _uiState.update { current ->
            if (current is EmergencyFlowState.AwaitingOTP) {
                current.copy(phoneNumber = newNumber)
            } else {
                current
            }
        }
    }

    fun verifyOtp(otpCode: String, onVerified: () -> Unit) {
        val state = _uiState.value
        if (state is EmergencyFlowState.AwaitingOTP) {
            _uiState.update { current ->
                if (current is EmergencyFlowState.AwaitingOTP) {
                    current.copy(isVerifying = true, otpError = null)
                } else {
                    current
                }
            }
            viewModelScope.launch {
                val success = otpRepository.verifyOtp(otpCode)
                if (success) {
                    otpTimerJob?.cancel()
                    _uiState.value = EmergencyFlowState.Confirmed(
                        location = state.location,
                        alertConfirmation = state.alertConfirmation,
                        fireStation = state.fireStation,
                        photoPath = state.photoPath,
                        verifiedPhoneNumber = state.phoneNumber
                    )
                    onVerified()
                } else {
                    _uiState.update { current ->
                        if (current is EmergencyFlowState.AwaitingOTP) {
                            current.copy(isVerifying = false, otpError = "Incorrect verification code. Please try again.")
                        } else {
                            current
                        }
                    }
                }
            }
        }
    }

    fun resetToHome() {
        otpTimerJob?.cancel()
        _uiState.value = EmergencyFlowState.Idle
    }
}
