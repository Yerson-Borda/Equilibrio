package com.example.moneymate.ui.screens.auth.singIn

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymate.R
import com.example.moneymate.ui.components.CustomButton
import com.example.moneymate.ui.components.CustomTextField
import com.example.moneymate.utils.Validation
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    viewModel: SignInViewModel = koinViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        launch {
            viewModel.showError.collect { error ->
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        launch {
            viewModel.navigateToHome.collect {
                onSignInSuccess()
            }
        }
    }

    SignInContent(
        onSignInClick = viewModel::signIn,
        onSignUpClick = onSignUpClick,
        isLoading = viewModel.isLoading.collectAsState().value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInContent(
    onSignInClick: (String, String) -> Unit,
    onSignUpClick: () -> Unit,
    isLoading: Boolean = false
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }
    val emailError = if (showErrors && !Validation.isValidEmail(email.value)) {
        "Email is invalid"
    } else {
        null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top section with logo and titles
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = stringResource(R.string.logo),
                    modifier = Modifier
                        .height(100.dp) // Reduced from 117dp
                        .width(160.dp)  // Reduced from 180dp
                )
                Spacer(Modifier.height(20.dp)) // Reduced from 26dp
                Text(
                    text = stringResource(R.string.welcome_back),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp)) // Reduced from 13dp
                Text(
                    text = stringResource(R.string.fill_all_inputs_for_logging_in),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            // Form section
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(16.dp)) // Reduced from 33dp

                CustomTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = stringResource(R.string.email),
                    iconResId = R.drawable.ic_email,
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier,
                    errorMessage = emailError
                )

                Spacer(modifier = Modifier.height(16.dp)) // Reduced from 20dp

                CustomTextField(
                    value = password.value,
                    onValueChange = { password.value = it },
                    label = stringResource(R.string.password),
                    iconResId = R.drawable.ic_lock,
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                    modifier = Modifier
                )

                Spacer(modifier = Modifier.height(24.dp)) // Reduced from 35dp

                CustomButton(
                    text = "SIGN IN",
                    onClick = {
                        showErrors = true
                        val isEmailValid = Validation.isValidEmail(email.value)
                        val isPasswordValid = password.value.isNotBlank()

                        if (isEmailValid && isPasswordValid) {
                            onSignInClick(email.value, password.value)
                        }
                    },
                    isLoading = isLoading,
                    backgroundColor = Color(0xFF4361EE)
                )
            }

            // Bottom section with links
            Column(
                modifier = Modifier.weight(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = stringResource(R.string.forgot_password),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable { /* Handle forgot password */ }
                )

                Spacer(modifier = Modifier.height(12.dp)) // Reduced from 17dp

                Row {
                    Text(
                        text = stringResource(R.string.don_t_have_an_account),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.sign_up),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4361EE),
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .clickable(onClick = onSignUpClick)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp)) // Reduced bottom spacing
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    MaterialTheme {
        SignInScreen(
            onSignUpClick = {},
            onSignInSuccess = {}
        )
    }
}