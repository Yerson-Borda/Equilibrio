package com.example.moneymate.ui.screens.auth.signUp

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymate.R
import com.example.moneymate.ui.components.CustomButton
import com.example.moneymate.ui.components.CustomTextField
import com.example.moneymate.ui.components.ErrorDialog
import com.example.moneymate.utils.Validation
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SignUpViewModel = koinViewModel(),
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    var showEmailExistsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        launch {
            viewModel.showError.collect { error ->
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }

        launch {
            viewModel.navigateToProfile.collect {
                onSignUpSuccess()
            }
        }

        launch {
            viewModel.showEmailExistsDialog.collect {
                showEmailExistsDialog = true
            }
        }
    }

    if (showEmailExistsDialog) {
        ErrorDialog(
            title = stringResource(R.string.not_possible_to_sign_up),
            message = stringResource(R.string.this_email_already_exists_try_to_log_in),
            onConfirm = {
                showEmailExistsDialog = false
                onNavigateToLogin()
            },
            onDismiss = {
                showEmailExistsDialog = false
            }
        )
    }

    SignUpContent(
        onSignUpClick = viewModel::signUp,
        onLoginClick = onLoginClick,
        isLoading = viewModel.isLoading.collectAsState().value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpContent(
    onSignUpClick: (String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean = false
) {
    val fullName = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    var showErrors by remember { mutableStateOf(false) }
    val emailError = if (showErrors && !Validation.isValidEmail(email.value)) {
        stringResource(R.string.email_is_invalid)
    } else {
        null
    }
    val passwordValidation = Validation.isValidPassword(password.value)
    val passwordError = if (showErrors) passwordValidation.errorMessage else null
    val passwordSupportText = if (!showErrors && password.value.isNotEmpty()) passwordValidation.errorMessage else null

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
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = stringResource(R.string.logo)
            )
            Spacer(Modifier.height(26.dp))
            Text(
                text = stringResource(R.string.welcome),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(13.dp))
            Text(
                text = stringResource(R.string.fill_all_inputs_for_registration),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(33.dp))

            CustomTextField(
                value = fullName.value,
                onValueChange = { fullName.value = it },
                label = stringResource(R.string.full_name),
                iconResId = R.drawable.ic_person,
                modifier = Modifier
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = stringResource(R.string.email),
                iconResId = R.drawable.ic_email,
                keyboardType = KeyboardType.Email,
                modifier = Modifier,
                errorMessage = emailError
            )

            Spacer(modifier = Modifier.height(20.dp))

            CustomTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = "Password",
                iconResId = R.drawable.ic_lock,
                isPassword = true,
                keyboardType = KeyboardType.Password,
                modifier = Modifier,
                errorMessage = passwordError,
                supportingText = passwordSupportText
            )

            Spacer(modifier = Modifier.height(35.dp))

            CustomButton(
                text = stringResource(R.string.sign_up),
                onClick = {
                    showErrors = true
                    val isEmailValid = Validation.isValidEmail(email.value)
                    val isPasswordValid = passwordValidation.isValid
                    val isFullNameValid = fullName.value.isNotBlank()

                    if (isEmailValid && isPasswordValid && isFullNameValid) {
                        onSignUpClick(fullName.value, email.value, password.value)
                    }
                },
                isLoading = isLoading,
                backgroundColor = Color(0xFF4361EE)
            )

            Spacer(modifier = Modifier.height(139.dp))

            Row {
                Text(
                    text = stringResource(R.string.already_have_account),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = stringResource(R.string.log_in),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4361EE),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable(onClick = onLoginClick)
                )
            }
        }
    }
}