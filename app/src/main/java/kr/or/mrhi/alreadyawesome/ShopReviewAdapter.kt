package kr.or.mrhi.alreadyawesome

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.or.mrhi.alreadyawesome.databinding.ItemShopReviewBinding

class ShopReviewAdapter(val context: Context, val reviewList: MutableList<Review>?) : RecyclerView.Adapter<ShopReviewAdapter.CustomViewHolder>() {
    // 리사이클러뷰에 아이템을 reviewList 전체가 아닌 3개만 출력하기 위해서 reviewList.size 대신 3을 입력
    override fun getItemCount(): Int {
        return 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemShopReviewBinding.inflate(LayoutInflater.from(context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = (holder as CustomViewHolder).binding
        // review에 들어있는 shopId에서 shopName을 찾기 위해서,
        // shopId -> shopList -> shop -> shopName 순으로 검색
        val dbHelper = DBHelper(context, MainActivity.DB_NAME, MainActivity.VERSION)
        val review = reviewList?.get(position)
        val shopList = dbHelper.selectShopById(review?.shopId)
        val shop = shopList?.get(0)

        binding.tvShopReviewName.text = shop?.shopName
        binding.tvShopReviewMenu.text = review?.menu
        binding.tvShopReviewRegisterId.text = review?.memberId
        binding.tvShopReviewDate.text = review?.date
        binding.tvShopReviewContent.text = review?.content
        binding.ratingBarShopReview.rating = review?.grade!!.toFloat()
    }

    // CustomViewHolder를 내부 클래스로 생성
    class CustomViewHolder(val binding: ItemShopReviewBinding) : RecyclerView.ViewHolder(binding.root)
}