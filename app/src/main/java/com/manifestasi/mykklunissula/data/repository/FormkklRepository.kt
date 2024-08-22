package com.manifestasi.mykklunissula.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.manifestasi.mykklunissula.presentation.pendaftarankkl.ScanType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FormkklRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val firebaseAuth: FirebaseAuth
) {
    suspend fun uploadImage(scanType: ScanType, imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val uid = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            val ref = firebaseStorage.reference.child("images/$uid/${scanType.name.lowercase()}_${System.currentTimeMillis()}.jpg")
            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        }
    }

    suspend fun saveDataToFirestore(data: Map<String, String?>,collection:String) {
        withContext(Dispatchers.IO) {
            firebaseFirestore.collection(collection).add(data).await()
        }
    }

}