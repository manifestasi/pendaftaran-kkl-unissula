package com.manifestasi.mykklunissula.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.manifestasi.mykklunissula.data.model.DataListPendaftar
import com.manifestasi.mykklunissula.data.repository.AuthRepository
import com.manifestasi.mykklunissula.data.repository.PendaftarRepository
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val pendaftarRepository: PendaftarRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    private val _resultKklDalamNegeri = MutableLiveData<Resource<DataListPendaftar>>()
    val resultKklDalamNegeri: LiveData<Resource<DataListPendaftar>> get() = _resultKklDalamNegeri

    fun getDataPendaftarKklDalamNegeri(idUser: String){
        viewModelScope.launch {
            _resultKklDalamNegeri.value = Resource.Loading
            try {
                val result = pendaftarRepository.getDataPendaftarKklDalamNegeri(idUser)
                _resultKklDalamNegeri.value = Resource.Success(result)
            } catch (e: Exception){
                _resultKklDalamNegeri.value = Resource.ErrorMessage(e.message.toString())
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    private val _resultKklLuarNegeri = MutableLiveData<Resource<DataListPendaftar>>()
    val resultKklLuarNegeri: LiveData<Resource<DataListPendaftar>> get() = _resultKklLuarNegeri

    fun getDataPendaftarKklLuarNegeri(idUser: String){
        viewModelScope.launch {
            _resultKklLuarNegeri.value = Resource.Loading
            try {
                val result = pendaftarRepository.getDataPendaftarKklLuarNegeri(idUser)
                _resultKklLuarNegeri.value = Resource.Success(result)
            } catch (e: Exception){
                _resultKklLuarNegeri.value = Resource.ErrorMessage(e.message.toString())
            }
        }
    }
}