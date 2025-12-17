import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymate.R
import com.example.moneymate.ui.components.CustomButton

@Composable
fun StartScreen(
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(495.dp)
                    .padding(top = 95.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.coint),
                    contentDescription = stringResource(R.string.left_coin),
                    modifier = Modifier
                        .size(70.dp)
                        .align(Alignment.TopStart)
                )

                Image(
                    painter = painterResource(id = R.drawable.group_1__2_),
                    contentDescription = stringResource(R.string.main_character),
                    modifier = Modifier
                        .size(280.dp, 495.dp)
                        .align(Alignment.BottomCenter)
                )

                Image(
                    painter = painterResource(id = R.drawable.donut),
                    contentDescription = stringResource(R.string.right_coin),
                    modifier = Modifier
                        .size(71.dp)
                        .align(Alignment.TopEnd)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.spend_smarter_save_more),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp,
                color = Color(0xFF4D6BFA)
            )

            Spacer(modifier = Modifier.height(26.dp))

            CustomButton(
                text = stringResource(R.string.get_started),
                onClick = onSignUpClick,
                backgroundColor = Color(0xFF4D6BFA)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(
                    text = stringResource(R.string.already_have_account),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                )
                Text(
                    text = stringResource(R.string.log_in),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4D6BFA),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .clickable(onClick = onLoginClick)
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    StartScreen(
        onSignUpClick = {},
        onLoginClick = {}
    )
}