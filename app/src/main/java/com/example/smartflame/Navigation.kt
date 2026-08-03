package com.example.smartflame

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartflame.data.repository.impl.GPSLocationRepository
import com.example.smartflame.ui.screens.*
import com.example.smartflame.viewmodel.EmergencyViewModel

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Scoped shared ViewModel across the entire navigation flow
    val viewModel: EmergencyViewModel = viewModel {
        EmergencyViewModel(
            locationRepository = GPSLocationRepository(context)
        )
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onEmergencyClick = {
                    viewModel.startEmergency()
                    navController.navigate("location_detecting")
                }
            )
        }
        
        composable("location_detecting") {
            LocationDetectingScreen(
                viewModel = viewModel,
                onResolved = {
                    navController.navigate("alert_sent") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
        
        composable("alert_sent") {
            AlertSentScreen(
                viewModel = viewModel,
                onNext = {
                    navController.navigate("photo_proof")
                },
                onCancelled = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
        
        composable("photo_proof") {
            PhotoProofScreen(
                viewModel = viewModel,
                onNext = {
                    navController.navigate("phone_verification")
                }
            )
        }
        
        composable("phone_verification") {
            PhoneVerificationScreen(
                viewModel = viewModel,
                onVerified = {
                    navController.navigate("final_confirmation") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
        
        composable("final_confirmation") {
            FinalConfirmationScreen(
                viewModel = viewModel,
                onExit = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
    }
}
