package com.kdd.kdd_frontend.navigation

/**
 * Definicion de las rutas de navegacion de la app.
 *
 * Cada objeto sellado (sealed object) representa una pantalla y su ruta unica.
 * Las rutas con argumentos (como planId o userId) usan el formato de plantilla
 * de Navigation Compose: "ruta/{argumento}".
 */
sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object EmailLogin : Screen("email_login")
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

    // Crear / Editar
    object CreatePlan : Screen("create_plan")
    object CreatePlanForCommunity : Screen("create_plan/{communityId}") {
        fun createRoute(communityId: Long) = "create_plan/$communityId"
    }
    object CreateCommunity : Screen("create_community")
    object EditPlan : Screen("edit_plan/{planId}") {
        fun createRoute(planId: Long) = "edit_plan/$planId"
    }

    // Chat
    object Chats : Screen("chats")
    object ChatDetail : Screen("chat_detail/{userId}/{nombre}") {
        fun createRoute(userId: Long, nombre: String) = "chat_detail/$userId/${android.net.Uri.encode(nombre)}"
    }

    // Perfil
    object Account : Screen("account")
    object EditProfile : Screen("edit_profile")
}
