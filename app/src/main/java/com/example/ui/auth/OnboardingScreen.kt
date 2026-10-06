package com.example.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.AppLanguage
import com.example.localization.StringsDefinition
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.theme.SafeRideBlue
import com.example.ui.theme.SafeRideGold
import com.example.ui.theme.SafeRideNavy

@Composable
fun OnboardingScreen(
    strings: StringsDefinition,
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSelectRoleForAuth: (role: String) -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAdminGateDialog by remember { mutableStateOf(false) }
    var adminAccessCodeInput by remember { mutableStateOf("") }
    var adminCodeError by remember { mutableStateOf<String?>(null) }
    var isCodeObscured by remember { mutableStateOf(true) }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = onLanguageSelected,
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showAdminGateDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminGateDialog = false
                adminCodeError = null
                adminAccessCodeInput = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = SafeRideNavy,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Platform Security Gateway",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = SafeRideNavy
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Restricted to Platform Owner Soham Pawar. Enter your Master Security Access Code to proceed:",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = adminAccessCodeInput,
                        onValueChange = {
                            adminAccessCodeInput = it
                            adminCodeError = null
                        },
                        label = { Text("Master Access Code") },
                        leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = SafeRideNavy) },
                        trailingIcon = {
                            IconButton(onClick = { isCodeObscured = !isCodeObscured }) {
                                Icon(
                                    imageVector = if (isCodeObscured) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle visibility",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        },
                        visualTransformation = if (isCodeObscured) PasswordVisualTransformation() else VisualTransformation.None,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_secret_code_input")
                    )
                    if (adminCodeError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = adminCodeError!!,
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = adminAccessCodeInput.trim()
                        val isValid = clean.equals("SP-8765", ignoreCase = true) ||
                                      clean == "7865" ||
                                      clean == "8765" ||
                                      clean.equals("soham", ignoreCase = true) ||
                                      clean == "Soham@Admin"
                        if (isValid) {
                            showAdminGateDialog = false
                            adminCodeError = null
                            adminAccessCodeInput = ""
                            onSelectRoleForAuth("SUPER_ADMIN")
                        } else {
                            adminCodeError = "Access Denied: Invalid Security Access Code."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeRideNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("admin_gate_confirm_btn")
                ) {
                    Text("Authorize Access", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAdminGateDialog = false
                        adminCodeError = null
                        adminAccessCodeInput = ""
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top bar with language selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .clickable { showLanguageDialog = true }
                    .testTag("onboarding_lang_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = SafeRideBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentLanguage.nativeName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SafeRideNavy
                    )
                }
            }

            // Privacy-First Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFDCFCE7),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF166534),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "100% Privacy Protected",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Image Asset
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.saferide_hero),
                contentDescription = "SafeRide Family Journey",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Logo & Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SafeRideBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "SafeRide",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "SAFE RIDE",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SafeRideNavy,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "“${strings.tagline}”",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF475569),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "CHOOSE YOUR ROLE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Role Option 1: Parent / Guardian
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectRoleForAuth("PARENT") }
                .testTag("role_parent_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, SafeRideBlue.copy(alpha = 0.3f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        tint = SafeRideBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.roleParent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SafeRideNavy
                    )
                    Text(
                        text = "Track your child's bus in real-time with peace of mind.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Role Option 2: Driver / Bus Owner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectRoleForAuth("DRIVER") }
                .testTag("role_driver_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = SafeRideGold,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.roleDriver,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SafeRideNavy
                    )
                    Text(
                        text = "Manage your vehicle, routes, Bus Code & student pickups.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Protected Admin Security Gateway (Protected by Master Code)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { showAdminGateDialog = true }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("admin_portal_link")
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = "Admin Security Gateway",
                tint = SafeRideNavy,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Platform Security Gateway",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SafeRideNavy
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
