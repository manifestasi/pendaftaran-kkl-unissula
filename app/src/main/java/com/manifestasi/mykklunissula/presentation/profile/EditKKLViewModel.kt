package com.manifestasi.mykklunissula.presentation.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manifestasi.mykklunissula.data.repository.FormkklRepository
import com.manifestasi.mykklunissula.presentation.pendaftarankkl.ScanType
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditKKLViewModel @Inject constructor(
    private val repository: FormkklRepository
) :ViewModel(){
    private val _uploadResult = MutableLiveData<Resource<Map<ScanType, String>>>()
    val uploadResult: LiveData<Resource<Map<ScanType, String>>> = _uploadResult

    private val _saveResult = MediatorLiveData<Resource<Void>>()
    val saveResult: LiveData<Resource<Void>> = _saveResult

    private val _dataResult = MutableLiveData<Resource<Map<String, Any>>>()
    val dataResult: LiveData<Resource<Map<String, Any>>> = _dataResult

    fun getDataFromFirestore() {
        viewModelScope.launch {
            _dataResult.value = Resource.Loading
            try {
                val data = repository.getDataFromFirestore()
                _dataResult.value = Resource.Success(data ?: emptyMap())
            } catch (e: Exception) {
                _dataResult.value = Resource.Error(e)
            }
        }
    }

    fun updateDataInFirestore(userId: String, data: Map<String, Any>) {
        viewModelScope.launch {
            _saveResult.value = Resource.Loading
            try {
                repository.updateDataInFirestore(userId, data)
                _saveResult.value = Resource.Success(null)
            } catch (e: Exception) {
                _saveResult.value = Resource.Error(e)
            }
        }
    }

    fun updateImage(scanType: ScanType, imageUri: Uri, previousUrl: String?) {
        viewModelScope.launch {
            _uploadResult.value = Resource.Loading
            try {
                val imageUrl = repository.updateImage(scanType, imageUri, previousUrl)
                _uploadResult.value = Resource.Success(mapOf(scanType to imageUrl))
            } catch (e: Exception) {
                _uploadResult.value = Resource.Error(e)
            }
        }
    }
}