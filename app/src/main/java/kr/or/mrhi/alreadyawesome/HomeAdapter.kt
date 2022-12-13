package kr.or.mrhi.alreadyawesome

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.or.mrhi.alreadyawesome.databinding.ItemRecommendShopBinding

class HomeAdapter(val context: Context, val shopList: MutableList<Shop>?) : RecyclerView.Adapter<HomeAdapter.CustomViewHolder>() {
    // 리사이클러뷰에 아이템을 shopList 전체가 아닌 3개만 출력하기 위해서 shopList.size 대신 3을 입력
    override fun getItemCount(): Int {
        return 3
    }

    // viewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemRecommendShopBinding.inflate(LayoutInflater.from(context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        // shopList속 해당 되는 position 속 shop 데이터를 가져옴
        val shop = shopList?.get(position)
        // TextView와 ratingBar에 해당되는 shop 내용을 뿌려줌
        binding.tvItemRecommendShopName.text = shop?.shopName
        binding.ratingBarItemRecommend.rating = shop?.shopGrade!!
        // 파이어베이스 스트로지로부터 해당되는 위치에 해당되는 이미지를 가져옴
        insertImage(binding.ivItemRecommendShopImage, shop)

        // viewHolder 클릭 이벤트 리스너
        binding.root.setOnClickListener {
            // binding.root.context = MainActivity(MainActivity -> HomeFragment -> HomeAdapter)
            // MainActivity에서 ShopActivity로 이동
            val intent = Intent(binding.root.context, ShopActivity::class.java)
            // ShopActivity로 이동할 때, shop과 MainActivity.TAB_HOME를 가지고 감
            // shop -> 해당 viewHolder의 내용이 담겨있음
            // MainActivity.TAB_HOME -> ShopActivity가 종료된 후에 MainActivity의 어느 Fragment로 돌아와야하는지 확인하기 위함
            intent.putExtra("shop", shop)
            intent.putExtra("from", MainActivity.TAB_HOME)
            binding.root.context.startActivity(intent)
            // Intent로 ShopActivity로 넘어가면, MainActivity는 종료
            (context as MainActivity).finish()
        }
    }

    // 파이어베이스 스토리지로부터 이미지를 가져와서 해당하는 ImageView에 출력
    // 해당 함수는 HomeAdapter 뿐만 아니라 shopImage/로부터 이미지 파일을 가져오는 모든 어댑터에서 사용됨
    // 이유 : 중복을 줄이기 위해서
    fun insertImage(imageView: ImageView, shop: Shop?) {
        val shopDAO = ShopDAO()
        // shopImage 폴더 속에 있는 shop?.image에 해당되는 JPG 파일을 지정
        val imgRef = shopDAO.storage!!.reference.child("shopImage/${shop?.image}.jpg")
        imgRef.downloadUrl.addOnCompleteListener {
            // 성공할 경우, it.result(결과물)을 지정된 imageView에 출력
            if (it.isSuccessful) {
                Glide.with(context)
                    .load(it.result)
                    .into(imageView)
            }
        }
    }

    // CustomViewHolder를 내부 클래스로 생성
    class CustomViewHolder(val binding: ItemRecommendShopBinding) : RecyclerView.ViewHolder(binding.root)
}