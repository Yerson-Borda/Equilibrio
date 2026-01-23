package com.example.moneymate.ui.navigation

import StartScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.moneymate.ui.screens.auth.signUp.SignUpScreen
import com.example.moneymate.ui.screens.auth.singIn.SignInScreen
import com.example.moneymate.ui.screens.goal.CreateGoalScreen
import com.example.moneymate.ui.screens.goal.GoalDetailViewModel
import com.example.moneymate.ui.screens.goal.GoalDetailsScreen
import com.example.moneymate.ui.screens.goal.GoalScreen
import com.example.moneymate.ui.screens.goal.GoalsListScreen
import com.example.moneymate.ui.screens.home.HomeScreen
import com.example.moneymate.ui.screens.profile.editprofile.EditProfileScreen
import com.example.moneymate.ui.screens.profile.profileoptions.ProfileOptionsScreen
import com.example.moneymate.ui.screens.profile.settings.SettingsNavigationEvent
import com.example.moneymate.ui.screens.profile.settings.SettingsScreen
import com.example.moneymate.ui.screens.profile.settings.SettingsScreenViewModel
import com.example.moneymate.ui.screens.splash.SplashScreen
import com.example.moneymate.ui.screens.transaction.TransactionScreen
import com.example.moneymate.ui.screens.transaction.addtransaction.AddTransactionScreen
import com.example.moneymate.ui.screens.wallet.CreateWalletScreen
import com.example.moneymate.ui.screens.wallet.EditWalletScreen
import com.example.moneymate.ui.screens.wallet.WalletDetailScreen
import com.example.moneymate.ui.screens.wallet.WalletScreen
import org.koin.androidx.compose.koinViewModel

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
                    navController.navigate(NavigationItem.Home.route) {
                        popUpTo(NavigationItem.SignIn.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(NavigationItem.SignIn.route) {
                        popUpTo(NavigationItem.SignUp.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(NavigationItem.SignIn.route)
                }
            )
        }
        composable(NavigationItem.SignIn.route) {
            SignInScreen(
                onSignInSuccess = {
                    navController.navigate(NavigationItem.Home.route) {
                        popUpTo(NavigationItem.SignIn.route) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate(NavigationItem.SignUp.route)
                }
            )
        }

        composable(NavigationItem.Home.route) {
            HomeScreen(
                currentScreen = "home",
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
                onSeeAllBudget = {
                    // Handle see all budget action
                },
                onSeeAllTransactions = {
                    navController.navigate(NavigationItem.Transactions.route)
                },
                onProfileClick = {
                    navController.navigate(NavigationItem.Profile.route)
                }
            )
        }

        composable(NavigationItem.Transactions.route) {
            TransactionScreen(
                currentScreen = "transactions",
                onBackClick = {
                    // Navigate to home and clear back stack
                    navController.navigate(NavigationItem.Home.route) {
                        popUpTo(NavigationItem.Transactions.route) { inclusive = true }
                    }
                },
                onNavigationItemSelected = { route ->
                    when (route) {
                        "home" -> navController.navigate(NavigationItem.Home.route) {
                            popUpTo(NavigationItem.Transactions.route) { inclusive = true }
                        }
                        "wallets" -> navController.navigate(NavigationItem.Wallets.route) {
                            launchSingleTop = true
                        }
                        "goals" -> navController.navigate(NavigationItem.Goals.route) {
                            launchSingleTop = true
                        }
                    }
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
                    navController.popBackStack()
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

        // Updated GoalScreen with navController
        composable(NavigationItem.Goals.route) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                GoalScreen(
                    navController = navController,
                    currentScreen = "goals",
                    onNavigationItemSelected = { route ->
                        when (route) {
                            "home" -> navController.navigate(NavigationItem.Home.route)
                            "transactions" -> navController.navigate(NavigationItem.Transactions.route)
                            "wallets" -> navController.navigate(NavigationItem.Wallets.route)
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(
            route = NavigationItem.GoalDetail.route,
            arguments = listOf(navArgument("goalId") { type = NavType.IntType })
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getInt("goalId") ?: 0
            val detailViewModel: GoalDetailViewModel = koinViewModel()

            // This replaces the SavedStateHandle logic
            LaunchedEffect(goalId) {
                detailViewModel.initialize(goalId)
            }

            GoalDetailsScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavigationItem.CreateGoal.route) {
            val createViewModel: GoalDetailViewModel = koinViewModel()

            LaunchedEffect(Unit) {
                createViewModel.initialize(null) // Signal Create Mode
            }

            CreateGoalScreen(
                viewModel = createViewModel,
                onBack = { navController.popBackStack() }
            )
        }

// Goals List Screen (Already mostly correct, just ensure NavigationItem used)
        composable(NavigationItem.GoalsList.route) {
            GoalsListScreen(
                navController = navController,
                currentScreen = "goals",
                onNavigationItemSelected = { route ->
                    navController.navigate(route) { launchSingleTop = true }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavigationItem.AddTransaction.route) {
            AddTransactionScreen(
                onBackClick = { navController.popBackStack() },
                onAddTransaction = { navController.popBackStack() }
            )
        }

        composable(NavigationItem.Profile.route) {
            ProfileOptionsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEditProfileClick = {
                    navController.navigate(NavigationItem.EditProfile.route)
                },
                onBanksAndCardsClick = {
                    // Handle banks and cards
                },
                onPaymentPreferencesClick = {
                    // Handle payment preferences
                },
                onExportTransactionsClick = {
                    // Handle export transactions
                },
                onSettingsClick = {
                    navController.navigate(NavigationItem.Settings.route)
                },
                onMessageCenterClick = {
                    // Handle message center
                }
            )
        }

        composable(NavigationItem.EditProfile.route) {
            EditProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onUpdateClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavigationItem.Settings.route) {
            val viewModel = koinViewModel<SettingsScreenViewModel>()
            val navigationEvent by viewModel.navigationEvent.collectAsStateWithLifecycle()

            // Handle logout navigation
            LaunchedEffect(navigationEvent) {
                when (navigationEvent) {
                    is SettingsNavigationEvent.LogoutSuccess -> {
                        // Navigate to sign in and clear back stack
                        navController.navigate(NavigationItem.SignIn.route) {
                            popUpTo(0) { inclusive = true }
                        }
                        viewModel.clearNavigationEvent()
                    }
                    else -> {}
                }
            }

            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogout = {
                    // This is handled by the ViewModel now
                },
                onLanguageClick = {
                    // Handle language selection
                },
                onNotificationsClick = {
                    // Handle notifications
                },
                onContactUsClick = {
                    // Handle contact us
                },
                onPrivacyPolicyClick = {
                    // Handle privacy policy
                },
                onDataSharingClick = {
                    // Handle data sharing
                },
                viewModel = viewModel
            )
        }
    }
}