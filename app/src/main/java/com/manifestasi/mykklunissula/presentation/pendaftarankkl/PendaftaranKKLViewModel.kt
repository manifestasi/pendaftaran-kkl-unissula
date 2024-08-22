package com.manifestasi.mykklunissula.presentation.pendaftarankkl

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manifestasi.mykklunissula.data.repository.FormkklRepository
import com.manifestasi.mykklunissula.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendaftaranKKLViewModel @Inject constructor(
    private val formkklRepository: FormkklRepository
) :ViewModel(){
    private val _uploadResult = MutableLiveData<Resource<Map<ScanType, String>>>()
    val uploadResult: LiveData<Resource<Map<ScanType, String>>> = _uploadResult

    private val _saveResult = MediatorLiveData<Resource<Void>>()
    val saveResult: LiveData<Resource<Void>> = _saveResult

    fun uploadMultipleImages(imageUris: Map<ScanType, Uri?>) {
        viewModelScope.launch {
            _uploadResult.value = Resource.Loading
            val resultMap = mutableMapOf<ScanType, String>()

            try {
                imageUris.forEach { (scanType, uri) ->
                    uri?.let {
                        val imageUrl = formkklRepository.uploadImage(scanType, it)
                        resultMap[scanType] = imageUrl
                    }
                }
                _uploadResult.value = Resource.Success(resultMap)
            } catch (e: Exception) {
                _uploadResult.value = Resource.Error(e)
            }
        }
    }

    fun saveDataToFirestore(data: Map<String, String?>,collection:String) {
        viewModelScope.launch {
            _saveResult.value = Resource.Loading
            try {
                formkklRepository.saveDataToFirestore(data,collection)
                _saveResult.value = Resource.Success(null)
            } catch (e: Exception) {
                _saveResult.value = Resource.Error(e)
            }
        }
    }
}