package com.manifestasi.mykklunissula.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.manifestasi.mykklunissula.util.Resource
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    fun loginUser(nim: String, password: String): LiveData<Resource<FirebaseUser?>> {
        val resultLiveData = MutableLiveData<Resource<FirebaseUser?>>()
        resultLiveData.value = Resource.Loading

        firebaseAuth.signInWithEmailAndPassword("$nim@myuniversity.com", password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    resultLiveData.value = Resource.Success(firebaseAuth.currentUser)
                } else {
                    resultLiveData.value = Resource.Error(task.exception ?: Exception("Unknown error"))
                }
            }

        return resultLiveData
    }

    suspend fun registerUser(nim: String, password: String, confirmPass: String): AuthResult {
        return suspendCoroutine { continuation ->
            if (password == confirmPass){
                firebaseAuth.createUserWithEmailAndPassword("$nim@myuniversity.com", password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(task.result)
                        } else {
                            continuation.resumeWithException(task.exception ?: Exception("Unknown error"))
                        }
                    }
            } else {
                continuation.resumeWithException(Exception("Password dan confirm password tidak cocok"))
            }

        }
    }

    fun logoutUser(): Boolean {
        firebaseAuth.signOut()
        return firebaseAuth.currentUser == null
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}