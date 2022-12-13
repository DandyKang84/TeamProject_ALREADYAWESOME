package kr.or.mrhi.alreadyawesome

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.or.mrhi.alreadyawesome.databinding.ItemShopBinding

class ShopListAdapter (val context: Context, val shopType: String, val shopList: MutableList<Shop>?) : RecyclerView.Adapter<ShopListAdapter.ShopListViewHoler>() {
    override fun getItemCount(): Int {
        return shopList?.size?:0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopListViewHoler {
        val binding = ItemShopBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ShopListViewHoler(binding)
    }

    override fun onBindViewHolder(holder: ShopListViewHoler, position: Int) {
        val binding = holder.binding
        val shop = shopList?.get(position)
        binding.tvItemShopName.text = shop?.shopName
        binding.tvItemOpen.text = shop?.openTime
        binding.tvItemClose.text = shop?.closeTime
        binding.tvItemAddress.text = shop?.address
        binding.ratingBarShop.rating = shop?.shopGrade!!
        // HomeAdapter에 생성해두었던 Firebase storage로부터 이미지를 받아오는 함수를 사용
        val homeAdapter = HomeAdapter(context, shopList)
        homeAdapter.insertImage(binding.ivItemShopImage, shop)

        when(shop.type){
            HairActivity.HAIR_KO -> binding.tvItemShopMenu.text = "커트 : ${shop.price1}원~"
            NailActivity.NAIL_KO -> binding.tvItemShopMenu.text = "네일 : ${shop.price1}원~"
            MakeUpActivity.MAKEUP_KO -> binding.tvItemShopMenu.text = "디자이너 : ${shop.price3}원~"
            SkinCareActivity.SKINCARE_KO -> binding.tvItemShopMenu.text = "코스1 : ${shop.price1}원~"
        }

        // viewHolder를 클릭하면 shopActivity로 이동
        binding.root.setOnClickListener {
            val intent = Intent(binding.root.context, ShopActivity::class.java)
            intent.putExtra("shop", shop)
            intent.putExtra("from", shopType)
            binding.root.context.startActivity(intent)
        }
    }

    // CustomViewHolder를 내부 클래스로 생성
    class ShopListViewHoler(val binding : ItemShopBinding) : RecyclerView.ViewHolder(binding.root)
}