package com.manifestasi.mykklunissula.presentation.pendaftarkkl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manifestasi.mykklunissula.data.model.DataListPendaftar
import com.manifestasi.mykklunissula.data.repository.PendaftarRepository
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendaftarViewModel @Inject constructor(
    private val pendaftarRepository: PendaftarRepository
): ViewModel() {
    private val _resultPendaftar = MutableLiveData<Resource<HashMap<String, ArrayList<DataListPendaftar>>>>()
    val resultPendaftar: LiveData<Resource<HashMap<String, ArrayList<DataListPendaftar>>>> get() = _resultPendaftar

    fun getALlDataPendaftarFromFirestore(){
        viewModelScope.launch {
            _resultPendaftar.value = Resource.Loading
            try {
                val result = pendaftarRepository.getALlDataPendaftarFromFirestore()
                _resultPendaftar.value = Resource.Success(result)
            } catch (e: Exception){
                _resultPendaftar.value = Resource.ErrorMessage(e.message.toString())
            }
        }
    }
}