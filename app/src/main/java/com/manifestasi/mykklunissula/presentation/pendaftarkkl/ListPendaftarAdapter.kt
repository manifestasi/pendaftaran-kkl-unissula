package com.manifestasi.mykklunissula.presentation.pendaftarkkl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.manifestasi.mykklunissula.data.model.DataListPendaftar
import com.manifestasi.mykklunissula.databinding.ItemListPendaftarBinding

class ListPendaftarAdapter : ListAdapter<DataListPendaftar, ListPendaftarAdapter.ViewHolder>(
    DIFF_CALLBACK) {

    inner class ViewHolder(var binding: ItemListPendaftarBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListPendaftarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val getPendaftar = getItem(position)
        holder.binding.tvName.text = getPendaftar.name
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DataListPendaftar>(){
            override fun areItemsTheSame(
                oldItem: DataListPendaftar,
                newItem: DataListPendaftar,
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: DataListPendaftar,
                newItem: DataListPendaftar,
            ): Boolean {
                return oldItem.name == newItem.name
            }

        }
    }
}