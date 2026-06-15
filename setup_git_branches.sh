#!/bin/bash
# =============================================================
# KDD Frontend - Setup completo de ramas y commits
# Ejecutar desde Git Bash en: C:\Users\aniki\IdeaProjects\KDD\KDD_frontend
# IMPORTANTE: Cierra IntelliJ/Android Studio antes de ejecutar
# =============================================================

set -e
REPO="$(pwd)"

echo "=== KDD Frontend: Setup de ramas ==="
echo "Directorio: $REPO"

# Eliminar lock si existe
rm -f .git/index.lock

# Configuración git
git config user.name "manuel-ir"
git config user.email "minfarodriguez@gmail.com"
git remote set-url origin git@github.com:manuel-ir/KDD_frontend.git

# ─────────────────────────────────────────────────────────────
# 1. feature/setup-frontend
# Base del proyecto: Gradle, tema, navegación, componentes
# ─────────────────────────────────────────────────────────────
echo ""
echo ">> Rama: feature/setup-frontend"
git checkout feature/setup-frontend

git add .gitignore 2>/dev/null || true
git add build.gradle.kts settings.gradle.kts gradle.properties gradlew gradlew.bat
git add gradle/
git add app/build.gradle.kts
git add app/src/main/AndroidManifest.xml
git add app/src/main/res/
git add app/src/main/java/com/kdd/kdd_frontend/ui/theme/
git add app/src/main/java/com/kdd/kdd_frontend/MainActivity.kt
git add app/src/main/java/com/kdd/kdd_frontend/navigation/
git add app/src/main/java/com/kdd/kdd_frontend/ui/components/

git commit -m "setup: configuración inicial del proyecto Android

- Gradle (AGP 9.x, Kotlin 2.2, BOM 2026.02)
- AndroidManifest con permisos internet y edge-to-edge
- Tema KDD: colores corporativos, tipografía Material3
- Navegación: NavGraph + sealed class Screen con todas las rutas
- Componentes reutilizables: BottomNavBar, PlanCard, CommunityCard, CreateBottomSheet"

# ─────────────────────────────────────────────────────────────
# 2. feature/auth-firebase
# Login y registro con Google Sign-In vía Firebase
# ─────────────────────────────────────────────────────────────
echo ""
echo ">> Rama: feature/auth-firebase"
git checkout feature/auth-firebase

git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/auth/LoginScreen.kt
git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/auth/RegisterScreen.kt
git add app/google-services.json 2>/dev/null || true

git commit -m "feat(auth): pantallas de login y registro con Google Sign-In

- LoginScreen: formulario email/contraseña + botón Google Sign-In
- RegisterScreen: formulario de registro completo
- Integración Firebase Auth con Google Sign-In (idToken -> JWT backend)
- webClientId configurado para el proyecto Firebase"

# ─────────────────────────────────────────────────────────────
# 3. feature/ui-explorar-mapa
# Pantalla principal (mapa), explorar actividades y filtros
# ─────────────────────────────────────────────────────────────
echo ""
echo ">> Rama: feature/ui-explorar-mapa"
git checkout feature/ui-explorar-mapa

git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/main/MainScreen.kt
git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/explore/ExploreScreen.kt
git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/explore/FiltersScreen.kt

git commit -m "feat(ui): pantalla principal, explorar actividades y filtros

- MainScreen: mapa placeholder + TopBar (logo KDD, chat, perfil)
  BottomSheet '¿Qué quieres crear?' al pulsar el botón +
- ExploreScreen: listado de planes con PlanCard + filtros activos
- FiltersScreen: ordenar por fecha/distancia, rango de edad,
  distancia en km, categoría, sede (para comunidades)"

# ─────────────────────────────────────────────────────────────
# 4. feature/ui-perfil
# Pantalla de cuenta y edición de perfil
# ─────────────────────────────────────────────────────────────
echo ""
echo ">> Rama: feature/ui-perfil"
git checkout feature/ui-perfil

git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/profile/AccountScreen.kt
git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/profile/EditProfileScreen.kt

git commit -m "feat(ui): pantallas de perfil de usuario

- AccountScreen: datos del usuario, estadísticas, opciones de cuenta
- EditProfileScreen: edición de nombre, edad y descripción personal
  con diálogos inline para cada campo"

# ─────────────────────────────────────────────────────────────
# 5. feature/ui-comunidades
# Comunidades: listado, detalle y creación
# ─────────────────────────────────────────────────────────────
echo ""
echo ">> Rama: feature/ui-comunidades"
git checkout feature/ui-comunidades

git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/communities/CommunitiesScreen.kt
git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/communities/CommunityDetailScreen.kt
git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/communities/CreateCommunityScreen.kt

git commit -m "feat(ui): módulo de comunidades

- CommunitiesScreen: tabs Descubrir/Tú, filtros, CommunityCard
  Estado vacío cuando el usuario no pertenece a ninguna
- CommunityDetailScreen: info, planes de la comunidad, botones
  Compartir y Unirse en la barra inferior
- CreateCommunityScreen: formulario nombre, rango de edad,
  ubicación, descripción y foto"

# ─────────────────────────────────────────────────────────────
# 6. feature/ui-crear-plan
# Crear actividad y detalle de plan
# ─────────────────────────────────────────────────────────────
echo ""
echo ">> Rama: feature/ui-crear-plan"
git checkout feature/ui-crear-plan

git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/plan/CreatePlanScreen.kt
git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/plan/PlanDetailScreen.kt

git commit -m "feat(ui): crear actividad y detalle de plan

- CreatePlanScreen: título, categoría, descripción, fecha/hora con
  DatePicker y TimePicker nativos, sección 'Hasta' opcional
  (visible pero deshabilitada), selector de idiomas, rango de edad
- PlanDetailScreen: foto, tabs Información/Presente(N),
  fila Presente clicable que navega al tab, grid de participantes,
  barra inferior con Favorito, Compartir, Unirse y +1"

# ─────────────────────────────────────────────────────────────
# 7. feature/ui-chat  (NUEVA)
# Chat privado entre usuarios
# ─────────────────────────────────────────────────────────────
echo ""
echo ">> Rama: feature/ui-chat (nueva)"
git checkout -b feature/ui-chat 2>/dev/null || git checkout feature/ui-chat

git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/chat/ChatsScreen.kt
git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/chat/ChatDetailScreen.kt

git commit -m "feat(ui): módulo de chat privado

- ChatsScreen: listado de conversaciones con avatar, nombre,
  último mensaje y hora
- ChatDetailScreen: conversación con burbujas enviado/recibido,
  campo de texto + botón enviar, menú con 'Borrar amigo'
  y 'Reportar usuario'"

# ─────────────────────────────────────────────────────────────
# 8. feature/ui-calendario  (NUEVA)
# Calendario de actividades del usuario
# ─────────────────────────────────────────────────────────────
echo ""
echo ">> Rama: feature/ui-calendario (nueva)"
git checkout -b feature/ui-calendario 2>/dev/null || git checkout feature/ui-calendario

git add app/src/main/java/com/kdd/kdd_frontend/ui/screens/calendar/CalendarScreen.kt

git commit -m "feat(ui): calendario de actividades

- CalendarScreen: tabs Próximos/Favoritos
- Estado vacío con CTA para crear actividad
- Listado de planes propios con PlanCard
- Integración con BottomNavBar (botón + abre CreateBottomSheet)"

# ─────────────────────────────────────────────────────────────
# 9. develop
# Integración completa de todas las ramas
# ─────────────────────────────────────────────────────────────
echo ""
echo ">> Rama: develop (integración completa)"
git checkout develop

git add -A

git commit -m "feat: integración completa de todas las pantallas UI

Pantallas implementadas:
- Auth: Login (Google Sign-In + Firebase), Register
- Main: mapa placeholder + BottomSheet crear
- Explore: listado de planes + filtros avanzados
- Plan: CreatePlan (date/time pickers, idiomas) + PlanDetail
- Communities: listado, detalle y creación de comunidad
- Chat: listado de chats + detalle de conversación
- Calendar: actividades próximas y favoritas
- Profile: cuenta + edición de perfil

Componentes compartidos:
- CreateBottomSheet: '¿Qué quieres crear?' disponible en todas las pantallas
- PlanCard, CommunityCard, BottomNavBar con estado activo"

echo ""
echo "=============================================="
echo "  COMMITS CREADOS. Ahora ejecuta:"
echo ""
echo "  git push origin feature/setup-frontend"
echo "  git push origin feature/auth-firebase"
echo "  git push origin feature/ui-explorar-mapa"
echo "  git push origin feature/ui-perfil"
echo "  git push origin feature/ui-comunidades"
echo "  git push origin feature/ui-crear-plan"
echo "  git push origin feature/ui-chat"
echo "  git push origin feature/ui-calendario"
echo "  git push origin develop"
echo ""
echo "  O para empujar todas de una vez:"
echo "  git push origin --all"
echo "=============================================="
