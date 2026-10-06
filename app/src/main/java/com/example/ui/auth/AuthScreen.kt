package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.AuthState
import com.example.ui.theme.SafeRideBlue
import com.example.ui.theme.SafeRideGold
import com.example.ui.theme.SafeRideNavy
import com.example.ui.theme.StatusDanger

@Composable
fun AuthScreen(
    initialRole: String, // "PARENT", "DRIVER", "SUPER_ADMIN"
    authState: AuthState,
    uiMessage: String?,
    isAdminPasswordConfigured: Boolean = false,
    onLogin: (email: String, pass: String) -> Unit,
    onRegister: (name: String, email: String, pass: String, role: String, phone: String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onVerifyMfa: (code: String) -> Unit,
    onBackToOnboarding: () -> Unit
) {
    val isAdmin = initialRole == "SUPER_ADMIN"
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register

    // Login Form State
    var loginEmail by remember {
        mutableStateOf(
            if (initialRole == "SUPER_ADMIN") "soham.pawar.8765@gmail.com" else ""
        )
    }
    var loginPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    // Register Form State
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }

    // MFA State
    var mfaCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isAdmin) Color(0xFF0F172A) else Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Back button
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBackToOnboarding,
                modifier = Modifier.testTag("auth_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isAdmin) Color.White else SafeRideNavy
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (initialRole) {
                    "SUPER_ADMIN" -> "Admin Security Portal"
                    "DRIVER" -> "Driver / Operator Gateway"
                    else -> "Parent / Guardian Gateway"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAdmin) Color.White else SafeRideNavy
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // MFA CHALLENGE SCREEN IF ACTIVE
        if (authState is AuthState.RequiresMfa) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAdmin) Color(0xFF1E293B) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SafeRideGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SafeRideNavy,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Two-Factor Authentication",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAdmin) Color.White else SafeRideNavy
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enter your 6-digit authentication security token.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = mfaCode,
                        onValueChange = { if (it.length <= 6) mfaCode = it },
                        label = { Text("6-Digit Code") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mfa_input")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { onVerifyMfa(mfaCode.ifBlank { authState.tempChallengeCode }) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mfa_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SafeRideGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Verify & Access Admin", color = SafeRideNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
            return
        }

        // Lockout banner if rate limited
        if (authState is AuthState.LockedOut) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = Color(0xFFFEE2E2),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = authState.reason,
                    color = StatusDanger,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Error message banner
        if (uiMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = uiMessage,
                    color = Color(0xFF92400E),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Tab Row (For Non-Admin, offer Login & Register. For Admin, login only)
        if (!isAdmin) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = SafeRideBlue,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Sign In", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Register", fontWeight = FontWeight.Bold) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAdmin) Color(0xFF1E293B) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (selectedTab == 0 || isAdmin) {
                    // Sign In Form or Admin Setup
                    Text(
                        text = if (isAdmin) {
                            if (!isAdminPasswordConfigured) "Super Admin Setup • Soham Pawar" else "Super Admin Sign In • Soham Pawar"
                        } else "Welcome Back",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isAdmin) Color.White else SafeRideNavy
                    )
                    Text(
                        text = if (isAdmin) {
                            if (!isAdminPasswordConfigured) {
                                "Set your Master Password once. Once configured, you will only ever be asked for your username & password."
                            } else {
                                "Enter your Master Password to authenticate as Platform Director."
                            }
                        } else "Sign in to manage your student transportation.",
                        fontSize = 12.sp,
                        color = if (isAdmin) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (localValidationError != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = localValidationError!!,
                                color = StatusDanger,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = loginEmail,
                        onValueChange = {
                            loginEmail = it
                            localValidationError = null
                        },
                        label = { Text(if (isAdmin) "Super Admin ID / Email" else "Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = {
                            loginPassword = it
                            localValidationError = null
                        },
                        label = { Text(if (isAdmin) (if (!isAdminPasswordConfigured) "Set Master Password (min 6 chars)" else "Master Password") else "Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    if (isAdmin && !isAdminPasswordConfigured) {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                localValidationError = null
                            },
                            label = { Text("Confirm Master Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle confirm password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_confirm_password_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (isAdmin && !isAdminPasswordConfigured) {
                                if (loginPassword.length < 6) {
                                    localValidationError = "Master Password must be at least 6 characters."
                                    return@Button
                                }
                                if (loginPassword != confirmPassword) {
                                    localValidationError = "Passwords do not match. Please re-enter."
                                    return@Button
                                }
                            }
                            if (loginEmail.isBlank() || loginPassword.isBlank()) {
                                localValidationError = "Please enter both username and password."
                                return@Button
                            }
                            localValidationError = null
                            onLogin(loginEmail, loginPassword)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdmin) SafeRideGold else SafeRideBlue
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isAdmin) {
                                if (!isAdminPasswordConfigured) "Lock In Master Password & Enter Dashboard" else "Authenticate Admin"
                            } else "Sign In with Email",
                            color = if (isAdmin) SafeRideNavy else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    if (!isAdmin) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFCBD5E1)))
                            Text(
                                text = "OR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFCBD5E1)))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = onGoogleSignIn,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("google_sign_in_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF94A3B8))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Google Sign-In",
                                tint = SafeRideBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sign in with Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SafeRideNavy
                            )
                        }
                    }
                } else {
                    // Register Form
                    Text(
                        text = "Create Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SafeRideNavy
                    )
                    Text(
                        text = "Register as a ${if (initialRole == "DRIVER") "Driver / Transport Operator" else "Parent / Guardian"}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = regName,
                        onValueChange = { regName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_email_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = regPhone,
                        onValueChange = { regPhone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_phone_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_pass_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (regEmail.isNotBlank() && regPassword.isNotBlank() && regName.isNotBlank()) {
                                onRegister(regName, regEmail, regPassword, initialRole, regPhone)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reg_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SafeRideBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create SafeRide Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
