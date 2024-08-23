package com.manifestasi.mykklunissula.data.repository

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.manifestasi.mykklunissula.data.model.DataListPendaftar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PendaftarRepository @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) {
    suspend fun getALlDataPendaftarFromFirestore(): HashMap<String, ArrayList<DataListPendaftar>> = coroutineScope {
        val result = hashMapOf(
            TEMP to ArrayList<DataListPendaftar>(),
            ORIGINAL to ArrayList<DataListPendaftar>()
        )

        val deffered1 = async { firebaseFirestore.collection("daftar_KKLluarnegeri").get().await() }
        val deffered2 = async { firebaseFirestore.collection("daftar_KKLdalamnegeri").get().await() }

        val (snapshot, snapshot2) = awaitAll(deffered1, deffered2)

        // Tentukan panjang maksimum
        val maxSize = maxOf(snapshot.size(), snapshot2.size())

        // Iterasi secara zigzag
        for (i in 0 until maxSize) {
            if (i < snapshot.size()) {
                val document = snapshot.documents[i]
                val getData = document.data

                val dataPendaftar = DataListPendaftar(
                    id = UUID.randomUUID().toString(),
                    name = getData?.get("nama").toString()
                )

                result[TEMP]?.add(dataPendaftar)
                result[ORIGINAL]?.add(dataPendaftar)
            }

            if (i < snapshot2.size()) {
                val document = snapshot2.documents[i]
                val getData = document.data

                val dataPendaftar = DataListPendaftar(
                    id = UUID.randomUUID().toString(),
                    name = getData?.get("nama").toString()
                )

                result[TEMP]?.add(dataPendaftar)
                result[ORIGINAL]?.add(dataPendaftar)
            }
        }

        return@coroutineScope result
    }

    companion object {
        const val TEMP = "temp"
        const val ORIGINAL = "original"
    }
}