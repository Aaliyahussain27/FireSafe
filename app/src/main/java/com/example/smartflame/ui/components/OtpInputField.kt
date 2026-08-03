package com.example.smartflame.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartflame.theme.InteractiveTeal
import com.example.smartflame.theme.TextPrimary
import com.example.smartflame.theme.ElevatedCardBackground

@Composable
fun OtpInputField(
    otpLength: Int = 6,
    onOtpComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val codeList = remember { mutableStateListOf(*Array(otpLength) { "" }) }
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        for (i in 0 until otpLength) {
            OutlinedTextField(
                value = codeList[i],
                onValueChange = { newValue ->
                    // Filter to digits
                    val text = newValue.filter { it.isDigit() }
                    if (text.isNotEmpty()) {
                        codeList[i] = text.take(1)
                        if (i < otpLength - 1) {
                            focusRequesters[i + 1].requestFocus()
                        }
                    } else {
                        codeList[i] = ""
                    }
                    
                    val finalCode = codeList.joinToString("")
                    if (finalCode.length == otpLength) {
                        onOtpComplete(finalCode)
                    }
                },
                modifier = Modifier
                    .width(44.dp)
                    .height(56.dp)
                    .focusRequester(focusRequesters[i])
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace) {
                            if (codeList[i].isEmpty() && i > 0) {
                                codeList[i - 1] = ""
                                focusRequesters[i - 1].requestFocus()
                                true
                            } else {
                                codeList[i] = ""
                                false
                            }
                        } else {
                            false
                        }
                    },
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ElevatedCardBackground,
                    unfocusedContainerColor = ElevatedCardBackground,
                    focusedBorderColor = InteractiveTeal,
                    unfocusedBorderColor = Color(0xFF2A2A2A)
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        // Safe check to avoid crashing if empty
        if (focusRequesters.isNotEmpty()) {
            focusRequesters[0].requestFocus()
        }
    }
}
