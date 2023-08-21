package com.example.scanning

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.scanning.databinding.ItemPhotoBinding

class PhotoAdapter: RecyclerView.Adapter<PhotoAdapter.PhotoVH>() {
    private val mListData:ArrayList<Uri> = arrayListOf()
    fun setListData(list:List<Uri>){
        mListData.clear()
        mListData.addAll(list)
        notifyDataSetChanged()
    }
    fun setData(uri:Uri) {
        mListData.clear()
        mListData.add(uri)
        notifyDataSetChanged()
    }
    inner class PhotoVH(private val itemBinding:ItemPhotoBinding):RecyclerView.ViewHolder(itemBinding.root) {
        fun bindData(data:Uri){
            itemBinding.image.setImageURI(data)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PhotoVH(ItemPhotoBinding.inflate(
        LayoutInflater.from(parent.context),parent,false))

    override fun getItemCount() = mListData.size

    override fun onBindViewHolder(holder: PhotoVH, position: Int) {
        holder.bindData(mListData[position])
    }
}