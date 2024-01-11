package com.example.applemarket

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.applemarket.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    private val item: MarketItem? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(IntentKeys.EXTRA_ITEM, MarketItem::class.java)
        } else {
            intent?.getParcelableExtra(IntentKeys.EXTRA_ITEM)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()

        // 상단 뒤로가기 화살표 이미지 클릭 시 디테일 페이지 종료
        binding.ivDetailArrowBackBtn.setOnClickListener {
            intent.putExtra(IntentKeys.EXTRA_ITEM, item)
            setResult(RESULT_OK, intent)
            finish()
        }

        // 좋아요 버튼 클릭 리스너
        binding.ivDetailItemFavoriteBtn.setOnClickListener {
            // 클릭이 되어 있는 상태
            if (item?.isClicked == true) {
                binding.ivDetailItemFavoriteBtn.setImageResource(R.drawable.ic_recycler_empty_heart_btn)
                item?.isClicked = false
            } else { // 클릭이 안 되어 있는 상태
                // 스낵바 생성
                val snackbar = Snackbar.make(it,
                    getString(R.string.snackbar_detail_favorite_comment), Snackbar.LENGTH_SHORT)
                snackbar.animationMode = Snackbar.ANIMATION_MODE_FADE
                snackbar.show()

                binding.ivDetailItemFavoriteBtn.setImageResource(R.drawable.ic_recycler_fill_heart_btn)
                item?.isClicked = true
            }
        }
    }

    private fun initView() {
        item?.thumbnail?.let { binding.ivDetailItemThumbnail.setImageResource(it) }
        binding.tvDetailItemSeller.text = item?.seller
        binding.tvDetailItemSellerAddress.text = item?.sellerAddress
        binding.tvDetailItemTitle.text = item?.title
        binding.tvDetailItemDescription.text = item?.description
        binding.tvDetailItemPrice.text = "${DecimalFormat("#,###").format(item?.price)}원"

        if (item?.isClicked == true) {
            binding.ivDetailItemFavoriteBtn.setImageResource(R.drawable.ic_recycler_fill_heart_btn)
        } else {
            binding.ivDetailItemFavoriteBtn.setImageResource(R.drawable.ic_recycler_empty_heart_btn)
        }
    }
}