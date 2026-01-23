package com.example.moneymate.di

import com.example.domain.auth.usecase.IsUserSignedInUseCase
import com.example.domain.auth.usecase.IsUserSignedInUseCaseImpl
import com.example.domain.auth.usecase.SignInUseCase
import com.example.domain.auth.usecase.SignInUseCaseImpl
import com.example.domain.auth.usecase.SignUpUseCase
import com.example.domain.auth.usecase.SignUpUseCaseImpl
import com.example.domain.budget.usecase.GetCurrentBudgetUseCase
import com.example.domain.budget.usecase.UpdateBudgetUseCase
import com.example.domain.category.usecase.CreateCategoryUseCase
import com.example.domain.category.usecase.DeleteCategoryUseCase
import com.example.domain.category.usecase.GetCategoriesUseCase
import com.example.domain.category.usecase.GetExpenseCategoriesUseCase
import com.example.domain.category.usecase.GetIncomeCategoriesUseCase
import com.example.domain.categoryLimit.usecase.DeleteCategoryLimitUseCase
import com.example.domain.categoryLimit.usecase.GetCategoryLimitsUseCase
import com.example.domain.categoryLimit.usecase.UpdateCategoryLimitUseCase
import com.example.domain.goal.usecase.CreateGoalUseCase
import com.example.domain.goal.usecase.DeleteGoalUseCase
import com.example.domain.goal.usecase.GetGoalUseCase
import com.example.domain.goal.usecase.GetGoalsUseCase
import com.example.domain.goal.usecase.UpdateGoalUseCase
import com.example.domain.tag.usecase.CreateTagUseCase
import com.example.domain.tag.usecase.DeleteTagUseCase
import com.example.domain.tag.usecase.GetTagsUseCase
import com.example.domain.transaction.usecase.CreateTransactionUseCase
import com.example.domain.transaction.usecase.CreateTransferUseCase
import com.example.domain.transaction.usecase.GetAverageSpendingUseCase
import com.example.domain.transaction.usecase.GetCategorySummaryUseCase
import com.example.domain.transaction.usecase.GetMonthlyChartDataUseCase
import com.example.domain.transaction.usecase.GetMonthlyComparisonUseCase
import com.example.domain.transaction.usecase.GetRecentTransactionsUseCase
import com.example.domain.transaction.usecase.GetTopCategoriesCurrentMonthUseCase
import com.example.domain.transaction.usecase.GetTransactionsUseCase
import com.example.domain.transaction.usecase.GetTransferPreviewUseCase
import com.example.domain.transaction.usecase.GetWalletTransactionsUseCase
import com.example.domain.user.usecase.DeleteAvatarUseCase
import com.example.domain.user.usecase.GetUserDetailedUseCase
import com.example.domain.user.usecase.GetUserUseCase
import com.example.domain.user.usecase.LogoutUseCase
import com.example.domain.user.usecase.UpdateUserUseCase
import com.example.domain.user.usecase.UploadAvatarUseCase
import com.example.domain.wallet.usecase.CreateWalletUseCase
import com.example.domain.wallet.usecase.DeleteWalletUseCase
import com.example.domain.wallet.usecase.GetTotalBalanceUseCase
import com.example.domain.wallet.usecase.GetWalletDetailUseCase
import com.example.domain.wallet.usecase.GetWalletUseCase
import com.example.domain.wallet.usecase.GetWalletsUseCase
import com.example.domain.wallet.usecase.UpdateWalletUseCase
import com.example.domain.savingsGoal.usecase.GetCurrentSavingsGoalUseCase
import com.example.domain.savingsGoal.usecase.UpdateSavingsGoalUseCase
import com.example.domain.transaction.usecase.GetSavingsTrendsUseCase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val useCaseModule = module {
    factoryOf(::IsUserSignedInUseCaseImpl) { bind<IsUserSignedInUseCase>() }
    factoryOf(::SignUpUseCaseImpl) { bind<SignUpUseCase>() }
    factoryOf(::SignInUseCaseImpl) { bind<SignInUseCase>() }
    factory { GetUserDetailedUseCase(get()) }
    factory { GetWalletUseCase(get()) }
    factory { GetWalletsUseCase(get())}
    factory { CreateWalletUseCase(get()) }
    factory { GetTotalBalanceUseCase(get()) }
    factory { GetWalletDetailUseCase(get()) }
    factory { DeleteWalletUseCase(get()) }
    factory { UpdateWalletUseCase(get()) }
    factory { CreateTransferUseCase(get()) }
    factory { GetTransferPreviewUseCase(get()) }
    factory { CreateTransactionUseCase(get()) }
    factory { GetTransactionsUseCase(get()) }
    factory { GetUserUseCase(get()) }
    factory { UpdateUserUseCase(get()) }
    factory { UploadAvatarUseCase(get()) }
    factory { DeleteAvatarUseCase(get()) }
    factory { LogoutUseCase(get()) }
    single { GetWalletTransactionsUseCase(get()) }
    factory { GetCategoriesUseCase(get()) }
    factory { CreateCategoryUseCase(get()) }
    factory { GetIncomeCategoriesUseCase(get()) }
    factory { GetExpenseCategoriesUseCase(get()) }
    factory { DeleteCategoryUseCase(get()) }
    factory{GetMonthlyChartDataUseCase(get())}
    factory{GetRecentTransactionsUseCase(get())}
    factory{GetCategorySummaryUseCase(get())}
    factory{ GetMonthlyComparisonUseCase(get()) }
    factory{ GetCurrentBudgetUseCase(get()) }
    factory{ UpdateBudgetUseCase(get()) }
    factory{ CreateTagUseCase(get()) }
    factory{ DeleteTagUseCase(get()) }
    factory{ GetTagsUseCase(get()) }
    factory{ GetCategoryLimitsUseCase(get()) }
    factory{ UpdateCategoryLimitUseCase(get()) }
    factory{ DeleteCategoryLimitUseCase(get()) }
    factory{ GetAverageSpendingUseCase(get()) }
    factory{ GetTopCategoriesCurrentMonthUseCase(get()) }
    factory{ GetCurrentSavingsGoalUseCase(get()) }
    factory{ UpdateSavingsGoalUseCase(get()) }
    factory{ CreateGoalUseCase(get()) }
    factory{ DeleteGoalUseCase(get()) }
    factory{ GetGoalUseCase(get()) }
    factory{ GetGoalsUseCase(get()) }
    factory{ UpdateGoalUseCase(get()) }
    factory{ GetSavingsTrendsUseCase(get()) }

}