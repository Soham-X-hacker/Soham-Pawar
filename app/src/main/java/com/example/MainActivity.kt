package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.security.AuthState
import com.example.ui.admin.AdminMainScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.OnboardingScreen
import com.example.ui.driver.DriverMainScreen
import com.example.ui.parent.ParentMainScreen
import com.example.ui.theme.SafeRideTheme
import com.example.ui.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeRideApp()
        }
    }
}

@Composable
fun SafeRideApp(viewModel: MainViewModel = viewModel()) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val strings by viewModel.strings.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val liveLocation by viewModel.liveLocation.collectAsStateWithLifecycle()
    val isSimulationMode by viewModel.isSimulationMode.collectAsStateWithLifecycle()

    val vehicles by viewModel.allVehicles.collectAsStateWithLifecycle()
    val routes by viewModel.allRoutes.collectAsStateWithLifecycle()
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val trips by viewModel.allTrips.collectAsStateWithLifecycle()
    val connectionRequests by viewModel.allConnectionRequests.collectAsStateWithLifecycle()
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val safetyAlerts by viewModel.allSafetyAlerts.collectAsStateWithLifecycle()
    val auditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    val settings by viewModel.allSettings.collectAsStateWithLifecycle()
    val tripEvents by viewModel.recentTripEvents.collectAsStateWithLifecycle()
    val uiMessage by viewModel.uiMessage.collectAsStateWithLifecycle()
    val isAdminPasswordConfigured by viewModel.isAdminPasswordConfigured.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUiMessage()
        }
    }

    var authRoleTarget by remember { mutableStateOf<String?>(null) } // null = Onboarding, "PARENT", "DRIVER", "SUPER_ADMIN"

    val isAdminActive = (authState as? AuthState.Authenticated)?.user?.role == "SUPER_ADMIN"

    SafeRideTheme(isAdminTheme = isAdminActive) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (val state = authState) {
                    is AuthState.Unauthenticated, is AuthState.LockedOut -> {
                        val roleTarget = authRoleTarget
                        if (roleTarget == null) {
                            OnboardingScreen(
                                strings = strings,
                                currentLanguage = currentLanguage,
                                onLanguageSelected = { viewModel.setLanguage(it) },
                                onSelectRoleForAuth = { role -> authRoleTarget = role }
                            )
                        } else {
                            val activity = androidx.compose.ui.platform.LocalContext.current as android.app.Activity
                            AuthScreen(
                                initialRole = roleTarget,
                                authState = state,
                                uiMessage = uiMessage,
                                isAdminPasswordConfigured = isAdminPasswordConfigured,
                                onLogin = { email, pass -> viewModel.login(email, pass) },
                                onRegister = { name, email, pass, role, phone ->
                                    viewModel.register(name, email, pass, role, phone)
                                },
                                onGoogleSignIn = {
                                    viewModel.loginWithGoogle(activity, roleTarget)
                                },
                                onVerifyMfa = { code -> viewModel.verifyMfa(code) },
                                onBackToOnboarding = { authRoleTarget = null }
                            )
                        }
                    }

                    is AuthState.RequiresMfa -> {
                        AuthScreen(
                            initialRole = "SUPER_ADMIN",
                            authState = state,
                            uiMessage = uiMessage,
                            isAdminPasswordConfigured = isAdminPasswordConfigured,
                            onLogin = { _, _ -> },
                            onRegister = { _, _, _, _, _ -> },
                            onGoogleSignIn = {},
                            onVerifyMfa = { code -> viewModel.verifyMfa(code) },
                            onBackToOnboarding = { viewModel.logout() }
                        )
                    }

                    is AuthState.Authenticated -> {
                        val currentUser = state.user
                        when (currentUser.role) {
                            "PARENT" -> {
                                val myNotifications = viewModel.repository.getNotificationsForUser(currentUser.id)
                                    .collectAsStateWithLifecycle(initialValue = emptyList()).value

                                ParentMainScreen(
                                    user = currentUser,
                                    students = students,
                                    vehicles = vehicles,
                                    connectionRequests = connectionRequests,
                                    activeTrips = trips,
                                    tripEvents = tripEvents,
                                    notifications = myNotifications,
                                    liveLocation = liveLocation,
                                    isSimulationMode = isSimulationMode,
                                    strings = strings,
                                    currentLanguage = currentLanguage,
                                    onLanguageSelected = { viewModel.setLanguage(it) },
                                    onToggleSimulation = { viewModel.toggleSimulationMode(it) },
                                    onSubmitConnectionRequest = { code, sName, pick, drop, onSuccess ->
                                        viewModel.submitBusConnectionRequest(code, sName, pick, drop, onSuccess)
                                    },
                                    onReportSafetyIssue = { vId, sev, cat, desc ->
                                        viewModel.reportSafetyAlert(vId, sev, cat, desc, currentUser.id, currentUser.fullName, "PARENT")
                                    },
                                    onLogout = { viewModel.logout() }
                                )
                            }

                            "DRIVER", "OPERATOR" -> {
                                val myNotifications = viewModel.repository.getNotificationsForUser(currentUser.id)
                                    .collectAsStateWithLifecycle(initialValue = emptyList()).value

                                DriverMainScreen(
                                    user = currentUser,
                                    vehicles = vehicles,
                                    routes = routes,
                                    students = students,
                                    trips = trips,
                                    connectionRequests = connectionRequests,
                                    notifications = myNotifications,
                                    liveLocation = liveLocation,
                                    isSimulationMode = isSimulationMode,
                                    strings = strings,
                                    currentLanguage = currentLanguage,
                                    onLanguageSelected = { viewModel.setLanguage(it) },
                                    onToggleSimulation = { viewModel.toggleSimulationMode(it) },
                                    onStartTrip = { vId, rId ->
                                        viewModel.startTrip(vId, rId, currentUser.id, currentUser.fullName)
                                    },
                                    onMarkStudentPickup = { tId, sId, name, pId ->
                                        viewModel.markStudentPickup(tId, sId, name, pId)
                                    },
                                    onMarkSchoolArrival = { tId, vId ->
                                        viewModel.markSchoolArrival(tId, vId)
                                    },
                                    onMarkStudentDropOff = { tId, sId, name, pId ->
                                        viewModel.markStudentDropOff(tId, sId, name, pId)
                                    },
                                    onEndTrip = { tId ->
                                        viewModel.endTrip(tId, currentUser.fullName)
                                    },
                                    onApproveRequest = { reqId ->
                                        viewModel.approveConnectionRequest(reqId, currentUser.fullName)
                                    },
                                    onRejectRequest = { reqId ->
                                        viewModel.rejectConnectionRequest(reqId, currentUser.fullName)
                                    },
                                    onAddVehicle = { num, type, dName, phone, cap, school ->
                                        viewModel.addVehicle(num, type, dName, phone, cap, school, currentUser.id)
                                    },
                                    onRegenerateCode = { vId ->
                                        viewModel.regenerateBusCode(vId)
                                    },
                                    onReportSafetyIssue = { vId, sev, cat, desc ->
                                        viewModel.reportSafetyAlert(vId, sev, cat, desc, currentUser.id, currentUser.fullName, "DRIVER")
                                    },
                                    onLogout = { viewModel.logout() }
                                )
                            }

                            "SUPER_ADMIN" -> {
                                AdminMainScreen(
                                    user = currentUser,
                                    vehicles = vehicles,
                                    routes = routes,
                                    students = students,
                                    users = users,
                                    trips = trips,
                                    safetyAlerts = safetyAlerts,
                                    auditLogs = auditLogs,
                                    settings = settings,
                                    liveLocation = liveLocation,
                                    isSimulationMode = isSimulationMode,
                                    strings = strings,
                                    currentLanguage = currentLanguage,
                                    onLanguageSelected = { viewModel.setLanguage(it) },
                                    onToggleUserSuspension = { uId, curr ->
                                        viewModel.toggleUserSuspension(uId, curr)
                                    },
                                    onToggleVehicleStatus = { vId, curr ->
                                        viewModel.toggleVehicleStatus(vId, curr)
                                    },
                                    onResolveAlert = { aId ->
                                        viewModel.resolveSafetyAlert(aId)
                                    },
                                    onAddVehicle = { num, type, dName, phone, cap, school ->
                                        viewModel.addVehicle(num, type, dName, phone, cap, school, currentUser.id)
                                    },
                                    onUpdateAdminProfile = { name, phone ->
                                        viewModel.updateAdminProfile(name, phone)
                                    },
                                    onUpdateSetting = { key, value ->
                                        viewModel.updateSystemSetting(key, value)
                                    },
                                    onLogout = { viewModel.logout() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
