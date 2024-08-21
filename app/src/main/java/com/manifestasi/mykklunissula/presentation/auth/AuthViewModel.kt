package com.manifestasi.mykklunissula.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.manifestasi.mykklunissula.data.repository.AuthRepository
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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

    private val _registerResult = MutableLiveData<Resource<FirebaseUser?>>()
    val registerResult: LiveData<Resource<FirebaseUser?>> = _registerResult

    fun registerUser(nim: String, password: String, confirmPass: String) {
        viewModelScope.launch {
            _registerResult.value = Resource.Loading
            try {
                val result = authRepository.registerUser(nim, password, confirmPass)
                if (result.user != null) {
                    _registerResult.value = Resource.Success(result.user)
                } else {
                    _registerResult.value = Resource.ErrorMessage("Something went wrong")
                }
            } catch (e: Exception) {
                _registerResult.value = Resource.ErrorMessage(e.message.toString())
            }
        }
    }

}