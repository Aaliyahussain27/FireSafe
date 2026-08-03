package com.example.smartflame.viewmodel

import com.example.smartflame.data.model.AlertConfirmation
import com.example.smartflame.data.model.FireStation
import com.example.smartflame.data.model.LocationData

sealed class EmergencyFlowState {
    object Idle : EmergencyFlowState()
    object DetectingLocation : EmergencyFlowState()
    
    data class AlertSent(
        val location: LocationData,
        val alertConfirmation: AlertConfirmation,
        val fireStation: FireStation,
        val isCancelling: Boolean = false
    ) : EmergencyFlowState()
    
    data class AwaitingPhoto(
        val location: LocationData,
        val alertConfirmation: AlertConfirmation,
        val fireStation: FireStation,
        val photoPath: String? = null
    ) : EmergencyFlowState()
    
    data class AwaitingOTP(
        val location: LocationData,
        val alertConfirmation: AlertConfirmation,
        val fireStation: FireStation,
        val photoPath: String?,
        val phoneNumber: String,
        val otpError: String? = null,
        val isVerifying: Boolean = false,
        val countdownSeconds: Int = 30
    ) : EmergencyFlowState()
    
    data class Confirmed(
        val location: LocationData,
        val alertConfirmation: AlertConfirmation,
        val fireStation: FireStation,
        val photoPath: String?,
        val verifiedPhoneNumber: String
    ) : EmergencyFlowState()
}
