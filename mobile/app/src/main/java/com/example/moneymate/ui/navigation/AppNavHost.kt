package com.example.moneymate.ui.navigation

import SettingsScreen
import StartScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.langswap.ui.navigation.NavigationItem
import com.example.moneymate.ui.screens.auth.singIn.SignInScreen
import com.example.moneymate.ui.screens.auth.signUp.SignUpScreen
import com.example.moneymate.ui.screens.home.HomeScreen
import com.example.moneymate.ui.screens.profile.editprofile.EditProfileScreen
import com.example.moneymate.ui.screens.profile.profileoptions.ProfileOptionsScreen
import com.example.moneymate.ui.screens.splash.SplashScreen

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
                isFirstLogin = false, // Set to false after login
                currentScreen = "home", // Pass current screen for bottom nav
                onNavigationItemSelected = { route ->
                    when (route) {
                        "transactions" -> navController.navigate(NavigationItem.Transactions.route)
                        "wallets" -> navController.navigate(NavigationItem.Wallets.route)
                        "goals" -> navController.navigate(NavigationItem.Goals.route)
                    }
                },
                onAddRecord = {
                    // Handle add record action
                    // You might want to navigate to an "Add Record" screen or show a dialog
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
                isFirstLogin = false,
                currentScreen = "transactions", // Mark transactions as active
                onNavigationItemSelected = { route ->
                    when (route) {
                        "home" -> navController.navigate(NavigationItem.Home.route)
                        "wallets" -> navController.navigate(NavigationItem.Wallets.route)
                        "goals" -> navController.navigate(NavigationItem.Goals.route)
                    }
                },
                // ... other parameters
            )
        }

        composable(NavigationItem.Wallets.route) {
            HomeScreen(
                isFirstLogin = false,
                currentScreen = "wallets", // Mark wallets as active
                onNavigationItemSelected = { route ->
                    when (route) {
                        "home" -> navController.navigate(NavigationItem.Home.route)
                        "transactions" -> navController.navigate(NavigationItem.Transactions.route)
                        "goals" -> navController.navigate(NavigationItem.Goals.route)
                    }
                },
                // ... other parameters
            )
        }

        composable(NavigationItem.Goals.route) {
            HomeScreen(
                isFirstLogin = false,
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