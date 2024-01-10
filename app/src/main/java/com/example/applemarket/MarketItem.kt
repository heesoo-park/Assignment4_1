package com.example.applemarket

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MarketItem(
    val id: Int,
    val thumbnail: Int,
    val title: String,
    val description: String,
    val seller: String,
    val price: Int,
    val sellerAddress: String,
    var favoriteCount: Int,
    var chatCount: Int,
    var isClicked: Boolean = false
) : Parcelable
