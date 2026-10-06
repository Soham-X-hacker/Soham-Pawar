package com.example.security

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.UserEntity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseAuthService(private val context: Context) {

    val auth: FirebaseAuth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)

    // Mandatory: custom database ID instance
    val firestore: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<UserEntity> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val firebaseUser = result.user ?: return Result.failure(IllegalStateException("Authentication failed"))
            val userEntity = syncUserRoleInFirestore(firebaseUser, defaultRole = "PARENT")
            Result.success(userEntity)
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Email sign in failed", e)
            Result.failure(e)
        }
    }

    suspend fun registerWithEmail(
        email: String,
        pass: String,
        fullName: String,
        role: String,
        phone: String
    ): Result<UserEntity> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val firebaseUser = result.user ?: return Result.failure(IllegalStateException("Registration failed"))
            val userEntity = initializeUserInFirestore(
                uid = firebaseUser.uid,
                email = email.trim(),
                fullName = fullName,
                role = role,
                phone = phone
            )
            Result.success(userEntity)
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Registration failed", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(activity: Activity, selectedRole: String = "PARENT"): Result<UserEntity> {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            return Result.failure(IllegalStateException("Google Sign-In configuration missing: default_web_client_id not found"))
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        return try {
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val firebaseUser = authResult.user ?: return Result.failure(IllegalStateException("Google authentication failed"))
                val userEntity = syncUserRoleInFirestore(firebaseUser, defaultRole = selectedRole)
                Result.success(userEntity)
            } else {
                Result.failure(IllegalStateException("Unexpected credential format"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("FirebaseAuth", "Google Sign-In cancelled: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Google Sign-In error", e)
            Result.failure(e)
        }
    }

    suspend fun syncUserRoleInFirestore(firebaseUser: FirebaseUser, defaultRole: String = "PARENT"): UserEntity {
        val userDocRef = firestore.collection("users").document(firebaseUser.uid)
        val snapshot = try {
            userDocRef.get().await()
        } catch (e: Exception) {
            null
        }

        return if (snapshot != null && snapshot.exists()) {
            val role = snapshot.getString("role") ?: defaultRole
            val fullName = snapshot.getString("fullName") ?: firebaseUser.displayName ?: "SafeRide User"
            val phone = snapshot.getString("phone") ?: firebaseUser.phoneNumber ?: ""
            UserEntity(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                passwordHash = "FIREBASE_AUTH",
                role = role,
                fullName = fullName,
                phone = phone,
                isMfaEnabled = (role == "SUPER_ADMIN")
            )
        } else {
            initializeUserInFirestore(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                fullName = firebaseUser.displayName ?: "SafeRide User",
                role = defaultRole,
                phone = firebaseUser.phoneNumber ?: ""
            )
        }
    }

    private suspend fun initializeUserInFirestore(
        uid: String,
        email: String,
        fullName: String,
        role: String,
        phone: String
    ): UserEntity {
        val data = hashMapOf<String, Any>(
            "id" to uid,
            "email" to email,
            "fullName" to fullName,
            "role" to role,
            "phone" to phone,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            firestore.collection("users").document(uid).set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Failed to write user role to Firestore", e)
        }

        return UserEntity(
            id = uid,
            email = email,
            passwordHash = "FIREBASE_AUTH",
            role = role,
            fullName = fullName,
            phone = phone,
            isMfaEnabled = (role == "SUPER_ADMIN")
        )
    }

    suspend fun signOut() {
        auth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w("FirebaseAuth", "Failed to clear credential state", e)
        }
    }
}
