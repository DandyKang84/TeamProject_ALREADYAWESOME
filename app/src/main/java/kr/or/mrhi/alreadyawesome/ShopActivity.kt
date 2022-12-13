package kr.or.mrhi.alreadyawesome

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kr.or.mrhi.alreadyawesome.databinding.ActivityShopBinding

class ShopActivity : AppCompatActivity() {
    var shop: Shop? = null
    var from: String? = ""
    lateinit var binding: ActivityShopBinding
    lateinit var reviewAdapter : ShopReviewAdapter
    var reviewList: MutableList<Review>? = mutableListOf<Review>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent로 전달받은 shop값을 함수에 전달해서 화면에 데이터를 입력함
        if (intent.getParcelableExtra<Shop>("shop") != null) {
            shop = intent.getParcelableExtra<Shop>("shop")
            shopInfo(shop)
        }

        // from = MainActivity 또는 ShopListAdapter의 어디에서부터 출발했는지가 적혀있음
        // 출발한 위치로 다시 돌아가기 위함
        if (intent.getStringExtra("from") != null) {
            from = intent.getStringExtra("from")
        }

        // call 버튼을 누르면 해당 매장의 번호로 전화를 연결하기 위해서 이동
        binding.btnShopCall.setOnClickListener {
            if (!(binding.tvShopPhone.text == "등록된 전화번호가 없습니다")){
                val phoneNumber = binding.tvShopPhone.text.toString()
                val myUir = Uri.parse("tel:${phoneNumber}")
                val intent = Intent(Intent.ACTION_DIAL,myUir)
                startActivity(intent)
            // 매장 전화번호가 입력되지 않은 경우에는 토스트 메시지 출력
            } else {
                Toast.makeText(this ,"매장번호 준비중 입니다", Toast.LENGTH_SHORT).show()
            }
        }

        // reservation 버튼을 누르면 dialog창을 출력
        binding.btnShopReservation.setOnClickListener {
            val menuDialog = ReservationDialog(this)
            menuDialog.showDialog(shop!!)
        }

        // 리뷰 전체보기 버튼을 누르면 ReviewActivity로 이동
        binding.tvShopShopReview.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("shop", shop)
            intent.putExtra("from", from)
            startActivity(intent)
            finish()
        }

        // 레이아웃 하단에 위치한 RecyclerView에 데이터를 뿌려주기 위한 함수
        getReview()
    }

    // intent로부터 전달받은 shop 정보를 화면에 입력하기 위한 함수
    fun shopInfo(shop: Shop?) {
        binding.tvShopShopName.text = shop?.shopName
        binding.tvShopOpenTime.text = shop?.openTime
        binding.tvShopCloseTime.text = shop?.closeTime
        binding.tvShopPhone.text = shop?.shopPhone
        binding.tvShopAddress.text = shop?.address
        binding.tvShopInfo.text = shop?.information
        binding.ratingBarShop.rating = shop?.shopGrade!!

        // HomeAdapter에 생성해두었던 Firebase Storage로부터 이미지를 가져오는 함수를 사용함
        val shopList = mutableListOf<Shop>()
        val homeAdapter = HomeAdapter(this, shopList)
        homeAdapter.insertImage(binding.ivShopImage, shop)

        // 매장의 타입에 따라서 출력할 내용을 달리함
        when (shop.type) {
            HairActivity.HAIR_KO -> insertMenuPrice("커트", "파마", "염색", shop)
            NailActivity.NAIL_KO -> insertMenuPrice("네일", "패디", "케어", shop)
            MakeUpActivity.MAKEUP_KO -> insertMenuPrice("원장님", "수석실장님", "디자이너", shop)
            SkinCareActivity.SKINCARE_KO -> insertMenuPrice("코스1", "코스2", "코스3", shop)
        }
    }

    // 중복되는 코드를 생략하기 위한 함수
    fun insertMenuPrice(menu1: String, menu2: String, menu3: String, shop: Shop) {
        binding.tvShopMenu1.text = "$menu1 : ${shop.price1}원~"
        binding.tvShopMenu2.text = "$menu2 : ${shop.price2}원~"
        binding.tvShopMenu3.text = "$menu3 : ${shop.price3}원~"
    }

    // 리사이클러뷰에 평점이 높은 순으로 최대 3개의 리뷰를 출력하기 위한 함수
    fun getReview() {
        val shopDAO = ShopDAO()
        shopDAO.selectReview()?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val review = data.getValue(Review::class.java)
                    if (review != null) {
                        // 리뷰 shopId와 ShopActivity의 shopId가 같은 경우에 한해서 추가
                        if (review.shopId == shop?.shopId) {
                            reviewList?.add(review)
                        }
                    }
                }
                // reviewlist의 정보를 grade 순으로 정렬
                reviewList?.sortBy { it.grade }
                // RecyclerView에 LinearLayoutManager와 Adpater를 연결
                reviewAdapter = ShopReviewAdapter(this@ShopActivity, reviewList)
                binding.rvShopReview.adapter = reviewAdapter
                binding.rvShopReview.layoutManager = LinearLayoutManager(this@ShopActivity)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("kr.or.mrhi", "ShopActivity selectReview() onCancelled")
            }
        })
    }

    // 백버튼을 누르면 intent로부터 전달받은 from에 따라서 해당 액티비티로 이동
    override fun onBackPressed() {
        when(from) {
            HairActivity.HAIR_KO -> {
                moveToActivity(HairActivity(), null)
            }
            NailActivity.NAIL_KO ->{
                moveToActivity(NailActivity(),  null)
            }
            MakeUpActivity.MAKEUP_KO -> {
                moveToActivity(MakeUpActivity(), null)
            }
            SkinCareActivity.SKINCARE_KO -> {
                moveToActivity(SkinCareActivity(), null)
            }
            MainActivity.TAB_HOME -> {
                moveToActivity(MainActivity(), MainActivity.TAB_HOME)
            }
            MainActivity.TAB_NEAR-> {
                moveToActivity(MainActivity(), MainActivity.TAB_NEAR)
            }
        }
    }

    // 액티비티를 움직이기 위한 함수
    // intent로부터 전달받은 from 값이 있으면 putExtra, 없으면 생략
    fun moveToActivity (activity: Activity, from: String?) {
        val intent = Intent(this, activity::class.java)
        if(from != null) {
            intent.putExtra("from", from)
        }
        startActivity(intent)
        finish()
    }
}