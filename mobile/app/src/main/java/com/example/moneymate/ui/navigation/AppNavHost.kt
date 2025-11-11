package com.example.moneymate.ui.navigation

import SettingsScreen
import StartScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.langswap.ui.navigation.NavigationItem
import com.example.moneymate.ui.screens.auth.singIn.SignInScreen
import com.example.moneymate.ui.screens.auth.signUp.SignUpScreen
import com.example.moneymate.ui.screens.home.HomeScreen
import com.example.moneymate.ui.screens.profile.editprofile.EditProfileScreen
import com.example.moneymate.ui.screens.profile.profileoptions.ProfileOptionsScreen
import com.example.moneymate.ui.screens.splash.SplashScreen
import com.example.moneymate.ui.screens.transaction.AddTransactionScreen
import com.example.moneymate.ui.screens.wallet.CreateWalletScreen
import com.example.moneymate.ui.screens.wallet.EditWalletScreen
import com.example.moneymate.ui.screens.wallet.WalletDetailScreen
import com.example.moneymate.ui.screens.wallet.WalletScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavigationItem.Splash.route,
        modifier = modifier
    ) {
        composable(NavigationItem.Splash.route) {
            SplashScreen(
                onStart = {
                    navController.navigate(NavigationItem.Start.route) {
                        popUpTo(NavigationItem.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(NavigationItem.Start.route) {
            StartScreen(
                onSignUpClick = {
                    navController.navigate(NavigationItem.SignUp.route)
                },
                onLoginClick = {
                    navController.navigate(NavigationItem.SignIn.route)
                }
            )
        }
        composable(NavigationItem.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(NavigationItem.Home.route)
                },
                onNavigateToLogin = {
                    navController.navigate(NavigationItem.SignIn.route) {
                        popUpTo(NavigationItem.SignUp.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate((NavigationItem.SignIn.route))
                }
            )
        }
        composable(NavigationItem.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(NavigationItem.Home.route)
                },
                onSignUpClick = {
                    navController.navigate(NavigationItem.SignUp.route)
                }
            )
        }

        composable(NavigationItem.Home.route) {
            HomeScreen(
                currentScreen = "home", // Pass current screen for bottom nav
                onNavigationItemSelected = { route ->
                    when (route) {
                        "transactions" -> navController.navigate(NavigationItem.Transactions.route)
                        "wallets" -> navController.navigate(NavigationItem.Wallets.route)
                        "goals" -> navController.navigate(NavigationItem.Goals.route)
                    }
                },
                onAddRecord = {
                    navController.navigate(NavigationItem.AddTransaction.route)
                },
                onAddWallet = {
                    // Handle add wallet action
                },
                onSeeAllBudget = {
                    // Handle see all budget action
                },
                onSeeAllTransactions = {
                    // Handle see all transactions action
                },
                onProfileClick = {
                    navController.navigate(NavigationItem.Profile.route)
                }
            )
        }

        // Add other screen destinations
        composable(NavigationItem.Transactions.route) {
            HomeScreen(
                currentScreen = "transactions", // Mark transactions as active
                onNavigationItemSelected = { route ->
                    when (route) {
                        "home" -> navController.navigate(NavigationItem.Home.route)
                        "wallets" -> navController.navigate(NavigationItem.Wallets.route)
                        "goals" -> navController.navigate(NavigationItem.Goals.route)
                    }
                },
                onAddRecord = {
                    navController.navigate(NavigationItem.AddTransaction.route)
                }
            )
        }

        composable(NavigationItem.Wallets.route) {
            WalletScreen(
                currentScreen = "wallets",
                onNavigateToWalletCreation = {
                    navController.navigate(NavigationItem.CreateWallet.route)
                },
                onNavigateToWalletDetail = { walletId ->
                    // Pass the walletId when navigating
                    navController.navigate(NavigationItem.WalletDetail.createRoute(walletId))
                },
                onNavigationItemSelected = { route ->
                    when (route) {
                        "home" -> navController.navigate(NavigationItem.Home.route)
                        "transactions" -> navController.navigate(NavigationItem.Transactions.route)
                        "goals" -> navController.navigate(NavigationItem.Goals.route)
                    }
                },
                onBackClick = {
                    navController.navigate(NavigationItem.Transactions.route)
                },
                onAddRecord = {
                    navController.navigate(NavigationItem.AddTransaction.route)
                }
            )
        }

        composable(
            NavigationItem.WalletDetail.route,
            arguments = listOf(navArgument("walletId") { type = NavType.IntType })
        ) { backStackEntry ->
            val walletId = backStackEntry.arguments?.getInt("walletId") ?: 0
            WalletDetailScreen(
                walletId = walletId,
                onBackClick = { navController.popBackStack() },
                onEditWallet = { walletId ->
                    navController.navigate(NavigationItem.EditWallet.createRoute(walletId))
                }
            )
        }

        composable(
            NavigationItem.EditWallet.route,
            arguments = listOf(navArgument("walletId") { type = NavType.IntType })
        ) { backStackEntry ->
            val walletId = backStackEntry.arguments?.getInt("walletId") ?: 0
            EditWalletScreen(
                walletId = walletId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavigationItem.CreateWallet.route) {
            CreateWalletScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavigationItem.CreateWallet.route) {
            CreateWalletScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavigationItem.Goals.route) {
            HomeScreen(
                currentScreen = "goals", // Mark goals as active
                onNavigationItemSelected = { route ->
                    when (route) {
                        "home" -> navController.navigate(NavigationItem.Home.route)
                        "transactions" -> navController.navigate(NavigationItem.Transactions.route)
                        "wallets" -> navController.navigate(NavigationItem.Wallets.route)
                    }
                },
                // ... other parameters
            )
        }

        composable(NavigationItem.AddTransaction.route) {
            AddTransactionScreen(
                onBackClick = { navController.navigate(NavigationItem.Home.route) },
                onAddTransaction = {navController.navigate(NavigationItem.Home.route)}
            )
        }

        composable(NavigationItem.Profile.route){
            ProfileOptionsScreen(
                onBackClick = {
                    navController.navigate(NavigationItem.Home.route)
                },
                onEditProfileClick = {
                    navController.navigate(NavigationItem.EditProfile.route)
                },
                onBanksAndCardsClick = {},
                onPaymentPreferencesClick = {},
                onExportTransactionsClick = {},
                onSettingsClick = {
                    navController.navigate(NavigationItem.Settings.route)
                }
            )
        }

        composable(NavigationItem.EditProfile.route){
            EditProfileScreen(
                onBackClick = {
                    navController.navigate(NavigationItem.Profile.route)
                },
                onUpdateClick = {
                    navController.navigate(NavigationItem.Profile.route)
                }
            )
        }

        composable(NavigationItem.Settings.route){
            SettingsScreen(
                onBackClick = {
                    navController.navigate(NavigationItem.Profile.route)
                },
                onLogoutClick = {
                    navController.navigate(NavigationItem.SignIn.route)
                },
                onLanguageClick = {},
                onNotificationsClick = {},
                onContactUsClick = {},
            )
        }
    }
}