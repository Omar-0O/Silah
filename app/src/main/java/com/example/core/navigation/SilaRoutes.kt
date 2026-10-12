package com.example.core.navigation

/**
 * Type-safe navigation routes for Sila application.
 */
sealed class Route(val route: String) {
    data object Splash : Route("splash")
    data object Onboarding : Route("onboarding")
    data object Dashboard : Route("dashboard")

    data class RelativeDetail(val id: Int = -1) : Route(ROUTE_PATTERN) {
        companion object {
            const val ROUTE_PATTERN = "relative_detail/{id}"
            const val ARG_ID = "id"
            fun createRoute(id: Int): String = "relative_detail/$id"
        }
    }

    data object Settings : Route("settings")
    data object Profile : Route("profile")
    data object WhyKinship : Route("why_kinship")
}
