package com.example.moneymate.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymate.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun WalletBalanceCard(
    viewModel: HomeViewModel = koinViewModel()
) {
    val totalBalance by viewModel.totalBalance.collectAsState()
    val isLoading by viewModel.isTotalBalanceLoading.collectAsState()

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
        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Total Balance",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "$${"%.2f".format(totalBalance?.totalBalance ?: 0.0)}",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = "Arrow icon",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FinancialOverviewCard(
    expenseCount: Int,
    incomeCount: Int
) {
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
                contentDescription = "Income",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Income",
                    color = Color(0xFFF3F3F3),
                    fontSize = 14.sp
                )
                Text(
                    text = "$incomeCount",
                    color = Color(0xFFFFFFFF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 12.dp)
                .width(1.dp)
                .height(50.dp)
                .background(Color(0xFFCFCFCF))
        )

        Row(
            modifier = Modifier.weight(1f)
                .padding(start = 24.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_spending),
                contentDescription = "Expenses",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Expenses",
                    color = Color(0xFFF3F3F3),
                    fontSize = 14.sp
                )
                Text(
                    text = "$expenseCount",
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