import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymate.R

@Composable
fun StartScreen(
    onGetStartedClicked: () -> Unit = {},
    onLoginClicked: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Background rectangle - covers entire screen
        Image(
            painter = painterResource(id = R.drawable.rectangle_1),
            contentDescription = "Background rectangle",
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 294.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Coin images and main character in a Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(495.dp)
                    .padding(top = 95.dp)
            ) {
                // Left coin - positioned at start
                Image(
                    painter = painterResource(id = R.drawable.coint),
                    contentDescription = "Left coin",
                    modifier = Modifier
                        .size(70.dp)
                        .align(Alignment.TopStart)
                )

                // Main character (man on box) - centered
                Image(
                    painter = painterResource(id = R.drawable.group_1__2_),
                    contentDescription = "Main character",
                    modifier = Modifier
                        .size(280.dp, 495.dp)
                        .align(Alignment.BottomCenter)
                )

                // Right coin - positioned at top end
                Image(
                    painter = painterResource(id = R.drawable.donut),
                    contentDescription = "Right coin",
                    modifier = Modifier
                        .size(71.dp)
                        .align(Alignment.TopEnd)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Main title
            Text(
                text = "Spend Smarter\nSave More",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Get Started button
            Button(
                onClick = onGetStartedClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login text
            Text(
                text = "Already Have Account Log In",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    StartScreen()
}