package com.example.moneymate.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymate.R

@Composable
fun WalletBalanceCard(totalBalance: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(115.dp)
            .background(
                color = Color(0xFF242424),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Total Balance",
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp
            )
            Text(
                text = "$${"%.2f".format(totalBalance)}",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier
                .padding(start = 252.dp, top = 71.dp, end = 24.dp, bottom = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically

        ) {
            Text(
                text = "My Wallet",
                color = Color(0xFFFFFFFF),
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                painter = painterResource(id = R.drawable.wallet),
                contentDescription = "Wallet icon",
                modifier = Modifier
                    .size(32.dp)
            )
        }
    }
}

@Composable
fun FinancialOverviewCard(savedAmount: Double, spentAmount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(66.dp)
            .background(
                color = Color(0xFF242424),
                shape = RoundedCornerShape(9.dp)
            )
    ) {
        Row(
            modifier = Modifier.weight(1f)
                .padding(start = 24.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_saving),
                contentDescription = "saved arrow",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Saved",
                    color = Color(0xFFF3F3F3),
                    fontSize = 14.sp
                )
                Text(
                    text = "$${"%.2f".format(savedAmount)}",
                    color = Color(0xFFFFFFFF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 12.dp , bottom = 12.dp)
                .width(1.dp)
                .height(50.dp)
                .background(Color(0xFFCFCFCF))
        )

        Row(
            modifier = Modifier.weight(1f)
                .padding(start = 70.dp , top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_spending),
                contentDescription = "spending arrow",
                modifier = Modifier
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Spending",
                    color = Color(0xFFF3F3F3),
                    fontSize = 14.sp
                )
                Text(
                    text = "$${"%.2f".format(spentAmount)}",
                    color = Color(0xFFFFFFFF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
//@Preview(showBackground = true, backgroundColor = 0xFF121212)
//@Composable
//fun WalletBalanceCardRegularPreview() {
//    MaterialTheme {
//        WalletBalanceCard()
//    }
//}
//@Preview(showBackground = true, backgroundColor = 0xFF121212)
//@Composable
//fun FinancialOverviewCardRegularPreview() {
//    MaterialTheme {
//        FinancialOverviewCard()
//    }
//}