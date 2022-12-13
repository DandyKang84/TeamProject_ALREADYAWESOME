package kr.or.mrhi.alreadyawesome

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.or.mrhi.alreadyawesome.databinding.ItemReviewBinding

class ReviewAdapter(val context: Context, val reviewList: MutableList<Review>?,val shopName : String?) : RecyclerView.Adapter<ReviewAdapter.CustomViewHolder>() {
    override fun getItemCount(): Int {
        return reviewList?.size?:0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = (holder as CustomViewHolder).binding
        val review = reviewList?.get(position)

        binding.tvItemReviewShopName.text = shopName
        binding.tvItemReviewMenu.text = review?.menu
        binding.tvItemReviewRegisterId.text = review?.memberId
        binding.tvItemReviewDate.text = review?.date
        binding.tvItemReviewContent.text = review?.content
        binding.ratingBarItemReview.rating = review?.grade!!.toFloat()

        // Firebase의 Storage로부터 reviewImage를 가져옴
        // 이미지가 없을 경우에는 visibility는 gone, 있을 경우에는 visible이 됨
        val shopDAO = ShopDAO()
        val imgRef = shopDAO.storage!!.reference.child("reviewImage/${review.reviewKey}.jpg")
        imgRef.downloadUrl.addOnCompleteListener {
                if (it.isSuccessful) {
                    Glide.with(context)
                        .load(it.result)
                        .into(binding.ivReviewRegisterPicture)
                    binding.ivReviewRegisterPicture.visibility = View.VISIBLE
                }else {
                    binding.ivReviewRegisterPicture.visibility = View.GONE
                }
        }
    }

    class CustomViewHolder(val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root)
}