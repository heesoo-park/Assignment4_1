package com.example.applemarket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.applemarket.databinding.ItemRecyclerviewBinding
import java.text.DecimalFormat

class ItemAdapter(private val items: MutableList<MarketItem>) :
    RecyclerView.Adapter<ItemAdapter.Holder>() {

    // 리사이클러뷰 아이템 클릭을 위한 인터페이스
    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    // 리사이클러뷰 아이템 롱클릭을 위한 인터페이스
    interface ItemLongClick {
        fun onLongClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null
    var itemLongClick: ItemLongClick? = null

    // ViewHolder를 만드는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            ItemRecyclerviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    // ViewHolder와 데이터를 연결시키는 함수
    override fun onBindViewHolder(holder: Holder, position: Int) {
        // 아이템 클릭 리스너
        holder.constraintLayoutItem.setOnClickListener {
            itemClick?.onClick(it, position)
        }

        // 아이템 롱클릭 리스너
        holder.constraintLayoutItem.setOnLongClickListener {
            itemLongClick?.onLongClick(it, position)

            return@setOnLongClickListener true
        }

        holder.ivItemThumbnail.setImageResource(items[position].thumbnail)
        holder.tvItemTitle.text = items[position].title
        holder.tvItemSellerAddress.text = items[position].sellerAddress
        holder.tvItemPrice.text = "${DecimalFormat("#,###").format(items[position].price)}원"
        holder.tvItemChatCount.text = items[position].chatCount.toString()
        if (items[position].isClicked) {
            holder.ivItemFavorite.setImageResource(R.drawable.ic_recycler_fill_heart_btn)
            holder.tvItemFavoriteCount.text = (items[position].favoriteCount + 1).toString()
        } else {
            holder.ivItemFavorite.setImageResource(R.drawable.ic_recycler_empty_heart_btn)
            holder.tvItemFavoriteCount.text = items[position].favoriteCount.toString()
        }
    }

    // 아이템 개수를 반환하는 함수
    override fun getItemCount(): Int {
        return items.size
    }

    // 리사이클러뷰 아이템들을 넣어둘 ViewHolder
    inner class Holder(private val binding: ItemRecyclerviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val constraintLayoutItem = binding.constraintLayoutRecyclerItem
        val ivItemThumbnail = binding.ivRecyclerItemThumbnail
        val tvItemTitle = binding.tvRecyclerItemTitle
        val tvItemSellerAddress = binding.tvRecyclerItemSellerAddress
        val tvItemPrice = binding.tvRecyclerItemPrice
        val tvItemChatCount = binding.tvRecyclerItemChatCount
        val ivItemFavorite = binding.ivRecyclerItemFavorite
        val tvItemFavoriteCount = binding.tvRecyclerItemFavoriteCount
    }
}