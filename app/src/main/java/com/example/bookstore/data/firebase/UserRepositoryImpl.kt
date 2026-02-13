package com.example.bookstore.data.firebase

import com.example.bookstore.domain.model.UserProfile
import com.example.bookstore.domain.model.UserSession
import com.example.bookstore.domain.repository.UserRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override val session: Flow<UserSession?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { a ->
            trySend(a.currentUser.toSession())
        }
        auth.addAuthStateListener(listener)
        trySend(auth.currentUser.toSession())
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun currentUid(): String? = auth.currentUser?.uid

    override suspend fun login(email: String, password: String) {
        mapAuthErrors {
            auth.signInWithEmailAndPassword(email, password).await()
            runCatching { ensureProfile() }
        }
    }

    override suspend fun register(name: String, email: String, password: String) {
        mapAuthErrors {
            auth.createUserWithEmailAndPassword(email, password).await()
            ensureProfile(name = name, email = email)
        }
    }

    override suspend fun sendPasswordReset(email: String) {
        mapAuthErrors {
            auth.sendPasswordResetEmail(email).await()
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun getProfile(): UserProfile? {
        val user = auth.currentUser ?: return null
        return runCatching {
            val snap = firestore.collection("users").document(user.uid).get().await()
            val name = snap.getString("name") ?: ""
            val email = snap.getString("email") ?: (user.email ?: "")
            val phone = snap.getString("phone") ?: ""
            val deliveryAddress = snap.getString("deliveryAddress") ?: ""
            UserProfile(
                uid = user.uid,
                name = name,
                email = email,
                phone = phone,
                deliveryAddress = deliveryAddress
            )
        }.getOrElse {
            UserProfile(
                uid = user.uid,
                name = "",
                email = user.email ?: "",
                phone = "",
                deliveryAddress = ""
            )
        }
    }

    override suspend fun updateProfile(name: String, phone: String, deliveryAddress: String) {
        val uid = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email.orEmpty()
        firestore.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "phone" to phone,
                    "deliveryAddress" to deliveryAddress
                )
            )
            .await()
    }

    private suspend fun ensureProfile(name: String? = null, email: String? = null) {
        val user = auth.currentUser ?: return
        val finalEmail = email ?: user.email.orEmpty()
        val ref = firestore.collection("users").document(user.uid)
        val snap = ref.get().await()
        if (snap.exists()) return
        val data = hashMapOf(
            "uid" to user.uid,
            "name" to (name ?: ""),
            "email" to finalEmail,
            "phone" to "",
            "deliveryAddress" to ""
        )
        ref.set(data).await()
    }

    private fun FirebaseUser?.toSession(): UserSession? {
        val u = this ?: return null
        return UserSession(uid = u.uid, email = u.email ?: "")
    }

    private suspend fun mapAuthErrors(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: FirebaseNetworkException) {
            throw IllegalStateException("Нет подключения к интернету. Операция недоступна в офлайн-режиме.")
        } catch (e: FirebaseAuthUserCollisionException) {
            throw IllegalStateException("Пользователь с таким email уже существует.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            throw IllegalStateException("Неверный email или пароль.")
        } catch (e: FirebaseAuthException) {
            throw IllegalStateException(e.localizedMessage ?: "Ошибка авторизации")
        }
    }
}
