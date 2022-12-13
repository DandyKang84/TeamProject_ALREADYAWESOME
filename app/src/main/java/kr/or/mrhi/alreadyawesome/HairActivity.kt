package kr.or.mrhi.alreadyawesome

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kr.or.mrhi.alreadyawesome.databinding.ActivityHairBinding
import kr.or.mrhi.alreadyawesome.databinding.DialogSortShopBinding

class HairActivity : AppCompatActivity() {
    companion object {
        const val HAIR_KO = "일반미용업"
        const val HAIR_EN = "HAIR"
    }
    lateinit var binding: ActivityHairBinding
    lateinit var shopListAdapter : ShopListAdapter
    lateinit var dialog : AlertDialog
    var shopList : MutableList<Shop>? = mutableListOf<Shop>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHairBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 액션바 대신 툴바 연결
        setSupportActionBar(binding.toolbar)
        // 자동적으로 생성되는 타이틀을 삭제
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Firebase로부터 매장 정보를 가져옴
        selectShop()

        // 리사이클러뷰에 LinearLayoutManager와 Adapter를 연결
        shopListAdapter = ShopListAdapter(applicationContext, HAIR_KO, shopList)
        binding.rvHair.layoutManager = LinearLayoutManager(this)
        binding.rvHair.adapter = shopListAdapter

        // 정렬기준 버튼을 클릭하면 다이얼로그 창이 출력
        binding.btnHairSort.setOnClickListener {
            // 어느 화면을 출력할 건지 선택
            val dialogBinding = DialogSortShopBinding.inflate(layoutInflater)
            // 어디에 출력할 건지 Builder를 생성
            val builder = AlertDialog.Builder(this)
            // 둘을 연결
            builder.setView(dialogBinding.root)
            // 이를 생성함
            dialog = builder.create()
            // 여백을 없애고, 다이얼로그창 모서리를 라운드 주도록 함
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            // 다이얼로그창이 중앙이 아닌 하단쪽에 출력되도록 함
            dialog.window!!.setGravity(Gravity.BOTTOM)
            // 다이얼로그창의 크기를 지정
            dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            dialog.show()
        }
    }

    // 화면이 전체적으로 가려졌을 때(=다른 액티비티로 이동했을 때) 액티비티를 종료함
    override fun onStop() {
        super.onStop()
        finish()
    }

    // Firebase로부터 shop 데이터를 가져옴
    fun selectShop() {
        val dbHelper = DBHelper(this, MainActivity.DB_NAME, MainActivity.VERSION)
        // 데이터를 SQLite에 넣기 위해서 SQLite의 shop을 초기화
        val deleteFlag = dbHelper.deleteShopAll()
        if (deleteFlag) {
            val shopDAO = ShopDAO()
            shopDAO.selectShop()?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // 데이터를 받을 shopList를 초기화
                    shopList?.clear()
                    for (shopData in snapshot.children) {
                        val shop = shopData.getValue(Shop::class.java)
                        if (shop != null) {
                            // 모든 shop데이터를 저장하지 않고, 해당되는 내용만 저장하도록 함수를 설정
                            typeCheck(shop)
                        }
                    }
                    Log.d("kr.or.mrhi", "HairActivity selectShop() onDataChange")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("kr.or.mrhi", "HairActivity selectShop() onCancelled")
                }
            })
        }
    }

    // 매장의 업태구분에 따라서 해당되는 것만 shopList에 저장함
    fun typeCheck(shop: Shop) {
        val dbHelper = DBHelper(this, MainActivity.DB_NAME, MainActivity.VERSION)
        if (shop.type == HAIR_KO){
            dbHelper.insertShop(shop)
            shopList?.add(shop)
        }
        shopListAdapter.notifyDataSetChanged()
    }

    // 오버플로우메뉴 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 직접 생성한 menu_overflow_category를 메뉴로 연결
        menuInflater.inflate(R.menu.menu_overflow_category, menu)
        // menu_overflow_category안에 있는 menuSearch를 searchView로 설정함
        val searchMenu = menu?.findItem(R.id.menuSearch)
        val searchView = searchMenu?.actionView as SearchView

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                val dbHelper = DBHelper(this@HairActivity, MainActivity.DB_NAME, MainActivity.VERSION)
                // 검색어가 입력되지 않았을 때는 전체리스트를 출력
                // toolbar의 타이틀 역할을 하는 tvHair에 문자를 입력
                if (query.isNullOrBlank()) {
                    binding.tvHair.text = HAIR_EN
                    shopList?.clear()
                    dbHelper.selectShopAll()?.let { shopList?.addAll(it) }
                    shopListAdapter.notifyDataSetChanged()
                } else {
                    // 검색어가 입력되었을 때는 그에 해당하는 리스트를 출력
                    // toolbar의 타이틀 역할을 하는 tvHair에 문자를 삭제함
                    binding.tvHair.text = ""
                    shopList?.clear()
                    dbHelper.selectShopByQuery(query)?.let { shopList?.addAll(it) }
                    shopListAdapter.notifyDataSetChanged()
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    // 백버튼을 누르면 MainActivity로 이동
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    // 다이얼로그창의 Textview를 누르면 하단의 함수를 작동됨
    fun clickSort(view: View) {
        when (view.id) {
            R.id.tvDialogSortLowPrice -> sortList(1)
            R.id.tvDialogSortHighPrice -> sortList(2)
            R.id.tvDialogSortGrade -> sortList(3)
        }
    }

    // sortType과 direction을 기준으로 shopList를 정렬함
    fun sortList(sortType: Int) {
        when (sortType) {
            1 -> shopList?.sortBy { it.price1 }
            2 -> shopList?.sortByDescending { it.price1 }
            3 -> shopList?.sortByDescending { it.shopGrade }
        }
        shopListAdapter.notifyDataSetChanged()
        dialog.dismiss()
    }
}