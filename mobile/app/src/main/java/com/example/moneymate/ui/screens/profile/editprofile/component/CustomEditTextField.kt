package com.example.moneymate.ui.screens.profile.editprofile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomEditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    leadingIcon: Int? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = if (enabled) Color(0xFF666666) else Color(0xFFAAAAAA),
                    fontSize = 16.sp
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = if (enabled) Color.Black else Color(0xFF888888),
                unfocusedTextColor = if (enabled) Color.Black else Color(0xFF888888),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = if (enabled) Color(0xFF4361EE) else Color.Transparent,
                focusedPlaceholderColor = if (enabled) Color(0xFF666666) else Color(0xFFAAAAAA),
                unfocusedPlaceholderColor = if (enabled) Color(0xFF666666) else Color(0xFFAAAAAA),
                disabledTextColor = Color(0xFF888888),
                disabledPlaceholderColor = Color(0xFFAAAAAA),
                disabledLeadingIconColor = Color(0xFFAAAAAA),
                disabledTrailingIconColor = Color(0xFFAAAAAA)
            ),
            shape = RoundedCornerShape(0.dp),
            singleLine = true,
            leadingIcon = {
                if (leadingIcon != null) {
                    Icon(
                        painter = painterResource(id = leadingIcon),
                        contentDescription = null,
                        tint = if (enabled) Color(0xFF666666) else Color(0xFFAAAAAA),
                        modifier = Modifier.size(23.dp)
                    )
                }
            },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword) {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        enabled = enabled
                    ) {
                        Icon(
                            painter = painterResource(id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility_on),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = if (enabled) Color(0xFF666666) else Color(0xFFAAAAAA)
                        )
                    }
                }
            },
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = if (enabled) Color.Black else Color(0xFF888888)
            ),
            enabled = enabled
        )

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                if (enabled) Color(0xFFF4F4F4) else Color(0xFFE0E0E0)
            )
        )
    }
}