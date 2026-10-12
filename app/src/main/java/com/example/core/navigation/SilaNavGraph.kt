package com.example.core.navigation

import android.content.Context
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.RelativeDetailScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WhyKinshipScreen
import com.example.viewmodel.RelativeViewModel

@Composable
fun SilaNavGraph(
    viewModel: RelativeViewModel,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Route.Splash.route
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(250))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(250))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(200))
        }
    ) {
        // Splash Screen
        composable(Route.Splash.route) {
            SplashScreen(
                onFinished = {
                    val onboardingDone = try {
                        prefs.getBoolean("onboarding_done", false)
                    } catch (e: Exception) {
                        false
                    }
                    val targetRoute = if (onboardingDone) Route.Dashboard.route else Route.Onboarding.route
                    navController.navigate(targetRoute) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding Screen
        composable(Route.Onboarding.route) {
            OnboardingScreen(
                viewModel = viewModel,
                onFinished = {
                    prefs.edit().putBoolean("onboarding_done", true).apply()
                    navController.navigate(Route.Dashboard.route) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Main Dashboard Screen
        composable(Route.Dashboard.route) {
            val selectedRelativeForDetail by viewModel.selectedRelativeForDetail.collectAsState()
            val showWhyKinshipScreen by viewModel.showWhyKinshipScreen.collectAsState()

            LaunchedEffect(selectedRelativeForDetail) {
                selectedRelativeForDetail?.let { relative ->
                    navController.navigate(Route.RelativeDetail.createRoute(relative.id))
                }
            }

            LaunchedEffect(showWhyKinshipScreen) {
                if (showWhyKinshipScreen) {
                    navController.navigate(Route.WhyKinship.route)
                }
            }

            MainDashboardScreen(
                viewModel = viewModel,
                onReplayOnboarding = {
                    navController.navigate(Route.Onboarding.route)
                }
            )
        }

        // Relative Detail Screen with Deep Linking
        composable(
            route = Route.RelativeDetail.ROUTE_PATTERN,
            arguments = listOf(
                navArgument(Route.RelativeDetail.ARG_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "sila://relative/{${Route.RelativeDetail.ARG_ID}}" },
                navDeepLink { uriPattern = "app://silah/relative/{${Route.RelativeDetail.ARG_ID}}" },
                navDeepLink { uriPattern = "https://silah.app/relative/{${Route.RelativeDetail.ARG_ID}}" }
            )
        ) { backStackEntry ->
            val relativeId = backStackEntry.arguments?.getInt(Route.RelativeDetail.ARG_ID) ?: -1
            val relatives by viewModel.relatives.collectAsState()
            val selectedRelative = relatives.find { it.id == relativeId }
                ?: viewModel.selectedRelativeForDetail.collectAsState().value

            if (selectedRelative != null) {
                val selectedLanguage by viewModel.selectedLanguage.collectAsState()
                val layoutDirection = if (selectedLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    RelativeDetailScreen(
                        relative = selectedRelative,
                        viewModel = viewModel,
                        onBack = {
                            viewModel.clearSelectedRelative()
                            navController.popBackStack()
                        }
                    )
                }
            } else {
                LaunchedEffect(Unit) {
                    viewModel.clearSelectedRelative()
                    navController.popBackStack()
                }
            }
        }

        // Why Kinship Educational Screen
        composable(Route.WhyKinship.route) {
            val selectedLanguage by viewModel.selectedLanguage.collectAsState()
            val layoutDirection = if (selectedLanguage == "en") LayoutDirection.Ltr else LayoutDirection.Rtl
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                WhyKinshipScreen(
                    lang = selectedLanguage,
                    onBack = {
                        viewModel.closeWhyKinshipScreen()
                        navController.popBackStack()
                    }
                )
            }
        }

        // Settings Screen
        composable(Route.Settings.route) {
            LaunchedEffect(Unit) {
                viewModel.openSettingsDialog()
                navController.popBackStack()
            }
        }

        // Profile Screen
        composable(Route.Profile.route) {
            MainDashboardScreen(
                viewModel = viewModel,
                onReplayOnboarding = {
                    navController.navigate(Route.Onboarding.route)
                }
            )
        }
    }
}
