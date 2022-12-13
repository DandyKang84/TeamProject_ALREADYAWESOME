package kr.or.mrhi.alreadyawesome

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.or.mrhi.alreadyawesome.databinding.ActivityReviewBinding
import kr.or.mrhi.alreadyawesome.databinding.DialogSortReviewBinding

class ReviewActivity : AppCompatActivity() {
    lateinit var binding: ActivityReviewBinding
    lateinit var reviewAdapter: ReviewAdapter
    var reviewList: MutableList<Review>? = mutableListOf<Review>()
    var shopList : MutableList<Shop>? = null
    var shop : Shop? = null
    var shopName :String? = null
    var from: String? = ""
    var shopId = ""
    var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent로 전달받은 shop값에서 shopId와 shopName을 구함
        if (intent.getParcelableExtra<Shop>("shop") != null) {
            shop = intent.getParcelableExtra<Shop>("shop")
        }
        shopId = shop?.shopId.toString()
        shopName = shop?.shopName

        // from = MainActivity 또는 ShopListAdapter의 어디에서부터 출발했는지가 적혀있음
        // 출발한 위치로 다시 돌아가기 위함
        if (intent.getStringExtra("from") != null) {
            from = intent.getStringExtra("from")
        }

        // RecyclerView에 LinearLayoutManger와 Apdater를 연결함
        val linearLayoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(this, reviewList, shopName)
        binding.reviewRecyclerView.layoutManager = linearLayoutManager
        binding.reviewRecyclerView.adapter = reviewAdapter

        // Firebase로부터 review을 가져옴
        selectReview()

        // 정렬 방법이 적힌 다이얼로그창을 출력
        binding.btnSort.setOnClickListener {
            val dialogBinding = DialogSortReviewBinding.inflate(layoutInflater)
            val builder = AlertDialog.Builder(this)
            builder.setView(dialogBinding.root)
            val dialog: AlertDialog = builder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setGravity(Gravity.BOTTOM)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog.show()

            // 리뷰를 낮은 평점순으로 정렬함
            // reviewList를 정렬한 후에 reviewAdpater에게 notify를 해서 데이터를 변경하도록 함
            dialogBinding.tvLowStar.setOnClickListener {
                reviewList?.sortBy { it.grade }
                reviewAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }

            // 리뷰를 높은 평점순으로 정렬함
            dialogBinding.tvHighStar.setOnClickListener {
                reviewList?.sortByDescending { it.grade }
                reviewAdapter.notifyDataSetChanged()
                dialog.dismiss()
            }

        }
        // 플로팅 버튼을 클릭하면 리뷰를 작성하는 ReviewAddActivity로 이동
        binding.reviewExtendFab.setOnClickListener {
            val intent = Intent(this, ReviewAddActivity::class.java)
            intent.putExtra("shop", shop)
            intent.putExtra("from", from)
            startActivity(intent)
            finish()
        }
    }

    // Firebase로부터 review 내용을 받아서 reviewList에 저장함
    fun selectReview() {
        val shopDAO = ShopDAO()
        shopDAO.selectReview()?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                reviewList?.clear()
                for (reviewData in snapshot.children){
                    val review = reviewData.getValue(Review::class.java)
                    if (review != null){
                        shopCheck(review)
                    }
                }
                Log.d("kr.or.mrhi","ReviewActivity selectReview(Firebase) onDataChange")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,"가져오기 실패", Toast.LENGTH_SHORT).show()
                Log.d("kr.or.mrhi","ReviewActivity selectReview(Firebase) onCancelled")
            }
        })
    }

    // Firebase로부터 전달되는 값 중 shopId가 같은 값만 reviewList에 더함(SQLite의 WHERE와 동일한 형태)
    // 먼저 작성한 리뷰가 아래 쪽으로, 최신 리뷰가 위로 올라가게 하기 위해서, 저장할 index 값을 0으로 지정함
    fun shopCheck(review: Review) {
        val shopCheck = review.shopId.toString()
        if (shopCheck == shopId){
            reviewList?.add(0, review)
        }
        reviewAdapter.notifyDataSetChanged()
    }

    // 해당 되는 매장에 달린 모든 리뷰의 평점을 평균 내서 shopGrade를 수정함
    fun updateShopGrade(reviewList: MutableList<Review>?) {
        // reviewList의 마지막 값을 maxPosition으로 지정
        val maxPosition = reviewList?.size!!
        // reviewList의 처음부터 마지막 값까지의 모든 점수를 가져와서 total에 더함
        for (position in 0 until maxPosition) {
            val grade = reviewList.get(position).grade
            total += grade
        }
        // total 값을 reviewList의 크기로 나눠서 평균을 구함
        val average = total / this.reviewList?.size!!.toFloat()
        val shopDAO = ShopDAO()
        // Firebase를 수정하기 위해서 값을 HashMap에 입력
        val hashMap : HashMap<String, Any?> = HashMap()
        hashMap["shopId"] = shopId
        hashMap["shopName"] = shopName
        hashMap["type"] = shop?.type
        hashMap["address"] = shop?.address
        hashMap["shopPhone"] = shop?.shopPhone
        hashMap["latitude"] = shop?.latitude
        hashMap["longitude"] = shop?.longitude
        hashMap["openTime"] = shop?.openTime
        hashMap["closeTime"] = shop?.closeTime
        hashMap["information"] = shop?.information
        hashMap["shopGrade"] = average
        hashMap["price1"] = shop?.price1
        hashMap["price2"] = shop?.price2
        hashMap["price3"] = shop?.price3
        hashMap["image"] = shop?.image
        // ShopActivity의 ratingbar를 수정하기 위해서 ShopActivity로 전달할 Shop의 값을 수정
        shop = Shop(shopId, shopName!!, shop?.type!!, shop?.address!!, shop?.shopPhone!!,
                shop?.latitude!!, shop?.longitude!!, shop?.openTime!!, shop?.closeTime!!, shop?.information!!,
                average, shop?.price1!!, shop?.price2!!, shop?.price3!!, shop?.image!!)
        // Firebase에 key값이 되는 shopId와 전체 값인 hashMap을 전달
        // 결과를 Log로 전달
        shopDAO.updateShop(shopId, hashMap).addOnSuccessListener {
            Log.d("kr.or.mrhi","ReviewActivity updateShop(Firebase) OnSuccess")
        }.addOnFailureListener {
            Log.d("kr.or.mrhi", "ReviewActivity updateShop(Firebase) OnFailure")
        }
    }

    // 백버튼을 클릭하면 shopGrade를 reviewList의 grade 평균으로 업데이트를 함
    // 이를 실행한 후에 ShopActivity로 이동
    override fun onBackPressed() {
        super.onBackPressed()
        val job = CoroutineScope(Dispatchers.Default).launch {
            if(reviewList != null) {
                updateShopGrade(reviewList)
            }
        }
        runBlocking {
            job.join()
        }
        val intent = Intent(this, ShopActivity::class.java)
        intent.putExtra("shop", shop)
        intent.putExtra("from", from)
        startActivity(intent)
    }
}