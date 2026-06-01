package com.kdd.kdd_frontend.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")

    // Main con bottom nav
    object Main : Screen("main")
    object Explore : Screen("explore")
    object Communities : Screen("communities")
    object Calendar : Screen("calendar")

    // Filtros
    object FiltersExplore : Screen("filters_explore")
    object FiltersCommunities : Screen("filters_communities")

    // Detalle
    object PlanDetail : Screen("plan_detail/{planId}") {
        fun createRoute(planId: Long) = "plan_detail/$planId"
    }
    object CommunityDetail : Screen("community_detail/{communityId}") {
        fun createRoute(communityId: Long) = "community_detail/$communityId"
    }

    // Crear
    object CreatePlan : Screen("create_plan")
    object CreateCommunity : Screen("create_community")

    // Chat
    object Chats : Screen("chats")
    object ChatDetail : Screen("chat_detail/{userId}") {
        fun createRoute(userId: Long) = "chat_detail/$userId"
    }

    // Perfil
    object Account : Screen("account")
    object EditProfile : Screen("edit_profile")
}
