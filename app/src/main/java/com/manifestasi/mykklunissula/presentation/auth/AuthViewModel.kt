package com.manifestasi.mykklunissula.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.manifestasi.mykklunissula.data.repository.AuthRepository
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) :ViewModel(){
    private val _loginResult = MutableLiveData<Resource<FirebaseUser?>>()
    val loginResult: LiveData<Resource<FirebaseUser?>> = _loginResult

    fun loginUser(nim: String, password: String) {
        _loginResult.value = Resource.Loading
        authRepository.loginUser(nim, password).observeForever { result ->
            _loginResult.value = result
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

}