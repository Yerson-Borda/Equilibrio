package com.example.moneymate.ui.navigation

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
    }
}