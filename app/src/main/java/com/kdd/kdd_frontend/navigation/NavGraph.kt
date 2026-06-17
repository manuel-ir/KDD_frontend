package com.kdd.kdd_frontend.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kdd.kdd_frontend.ui.screens.auth.LoginScreen
import com.kdd.kdd_frontend.ui.screens.auth.RegisterScreen
import com.kdd.kdd_frontend.ui.screens.calendar.CalendarScreen
import com.kdd.kdd_frontend.ui.screens.chat.ChatDetailScreen
import com.kdd.kdd_frontend.ui.screens.chat.ChatsScreen
import com.kdd.kdd_frontend.ui.screens.communities.CommunitiesScreen
import com.kdd.kdd_frontend.ui.screens.communities.CommunityDetailScreen
import com.kdd.kdd_frontend.ui.screens.communities.CreateCommunityScreen
import com.kdd.kdd_frontend.ui.screens.explore.ExploreScreen
import com.kdd.kdd_frontend.ui.screens.explore.FiltersScreen
import com.kdd.kdd_frontend.ui.screens.main.MainScreen
import com.kdd.kdd_frontend.ui.screens.plan.CreatePlanScreen
import com.kdd.kdd_frontend.ui.screens.plan.EditPlanScreen
import com.kdd.kdd_frontend.ui.screens.plan.PlanDetailScreen
import com.kdd.kdd_frontend.ui.screens.profile.AccountScreen
import com.kdd.kdd_frontend.ui.screens.profile.EditProfileScreen

/**
 * Grafo de navegacion de la aplicacion.
 *
 * Define todas las pantallas (destinos) de la app y las rutas entre ellas.
 * Usa Jetpack Navigation Compose para gestionar la pila de pantallas
 * sin necesidad de gestionar el BackStack manualmente.
 *
 * Pantallas principales:
 * - Login / Registro
 * - Mapa principal (pantalla de inicio)
 * - Explora, Calendario, Chats, Comunidades, Perfil
 * - Detalle de plan, crear plan, editar plan
 * - Detalle de comunidad, crear comunidad
 * - Chat con un usuario concreto
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Main.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        // Registro con email y contrasena
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Main.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Main
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToExplore = { navController.navigate(Screen.Explore.route) },
                onNavigateToCommunities = { navController.navigate(Screen.Communities.route) },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToCreatePlan = { navController.navigate(Screen.CreatePlan.route) },
                onNavigateToCreateCommunity = { navController.navigate(Screen.CreateCommunity.route) },
                onNavigateToChats = { navController.navigate(Screen.Chats.route) },
                onNavigateToAccount = { navController.navigate(Screen.Account.route) },
                onNavigateToPlan = { planId -> navController.navigate(Screen.PlanDetail.createRoute(planId)) }
            )
        }

        // Create Community
        composable(Screen.CreateCommunity.route) {
            CreateCommunityScreen(
                onNavigateBack = { navController.popBackStack() },
                onCommunityCreated = { navController.popBackStack() }
            )
        }

        // Explore
        composable(Screen.Explore.route) {
            ExploreScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlan = { planId -> navController.navigate(Screen.PlanDetail.createRoute(planId)) },
                onNavigateToFilters = { navController.navigate(Screen.FiltersExplore.route) },
                onNavigateToMain = { navController.popBackStack(Screen.Main.route, false) },
                onNavigateToCommunities = { navController.navigate(Screen.Communities.route) },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToCreatePlan = { navController.navigate(Screen.CreatePlan.route) },
                onNavigateToCreateCommunity = { navController.navigate(Screen.CreateCommunity.route) }
            )
        }
        composable(Screen.FiltersExplore.route) {
            FiltersScreen(
                onNavigateBack = { navController.popBackStack() },
                isCommunityFilter = false
            )
        }
        composable(Screen.FiltersCommunities.route) {
            FiltersScreen(
                onNavigateBack = { navController.popBackStack() },
                isCommunityFilter = true
            )
        }

        // Communities
        composable(Screen.Communities.route) {
            CommunitiesScreen(
                onNavigateToMain = { navController.popBackStack(Screen.Main.route, false) },
                onNavigateToCommunityDetail = { id -> navController.navigate(Screen.CommunityDetail.createRoute(id)) },
                onNavigateToFilters = { navController.navigate(Screen.FiltersCommunities.route) },
                onNavigateToExplore = { navController.navigate(Screen.Explore.route) },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToCreatePlan = { navController.navigate(Screen.CreatePlan.route) },
                onNavigateToCreateCommunity = { navController.navigate(Screen.CreateCommunity.route) }
            )
        }
        composable(
            route = Screen.CommunityDetail.route,
            arguments = listOf(navArgument("communityId") { type = NavType.LongType })
        ) { backStackEntry ->
            val communityId = backStackEntry.arguments?.getLong("communityId") ?: 0L
            CommunityDetailScreen(
                communityId = communityId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlan = { planId -> navController.navigate(Screen.PlanDetail.createRoute(planId)) },
                onNavigateToCreatePlan = { navController.navigate(Screen.CreatePlanForCommunity.createRoute(communityId)) }
            )
        }
        composable(
            route = Screen.CreatePlanForCommunity.route,
            arguments = listOf(navArgument("communityId") { type = NavType.LongType })
        ) { backStackEntry ->
            val communityId = backStackEntry.arguments?.getLong("communityId") ?: -1L
            CreatePlanScreen(
                onNavigateBack = { navController.popBackStack() },
                onPlanCreated = { planId ->
                    navController.navigate(Screen.PlanDetail.createRoute(planId)) {
                        popUpTo(Screen.CreatePlanForCommunity.route) { inclusive = true }
                    }
                },
                communityId = communityId
            )
        }

        // Plan Detail
        composable(
            route = Screen.PlanDetail.route,
            arguments = listOf(navArgument("planId") { type = NavType.LongType })
        ) { backStackEntry ->
            PlanDetailScreen(
                planId = backStackEntry.arguments?.getLong("planId") ?: 0L,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditPlan = { planId -> navController.navigate(Screen.EditPlan.createRoute(planId)) }
            )
        }

        // Create Plan
        composable(Screen.CreatePlan.route) {
            CreatePlanScreen(
                onNavigateBack = { navController.popBackStack() },
                onPlanCreated = { planId ->
                    navController.navigate(Screen.PlanDetail.createRoute(planId)) {
                        popUpTo(Screen.CreatePlan.route) { inclusive = true }
                    }
                }
            )
        }

        // Edit Plan
        composable(
            route = Screen.EditPlan.route,
            arguments = listOf(navArgument("planId") { type = NavType.LongType })
        ) { backStackEntry ->
            EditPlanScreen(
                planId = backStackEntry.arguments?.getLong("planId") ?: 0L,
                onNavigateBack = { navController.popBackStack() },
                onPlanEditado = { navController.popBackStack() }
            )
        }

        // Calendar
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateToMain = { navController.popBackStack(Screen.Main.route, false) },
                onNavigateToExplore = { navController.navigate(Screen.Explore.route) },
                onNavigateToCommunities = { navController.navigate(Screen.Communities.route) },
                onNavigateToCreatePlan = { navController.navigate(Screen.CreatePlan.route) },
                onNavigateToCreateCommunity = { navController.navigate(Screen.CreateCommunity.route) },
                onNavigateToPlan = { planId -> navController.navigate(Screen.PlanDetail.createRoute(planId)) }
            )
        }

        // Chats
        composable(Screen.Chats.route) {
            ChatsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChatDetail = { userId, nombre -> navController.navigate(Screen.ChatDetail.createRoute(userId, nombre)) }
            )
        }
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.LongType },
                navArgument("nombre") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ChatDetailScreen(
                userId = backStackEntry.arguments?.getLong("userId") ?: 0L,
                nombre = backStackEntry.arguments?.getString("nombre") ?: "",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Profile
        composable(Screen.Account.route) {
            AccountScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
