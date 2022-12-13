package kr.or.mrhi.alreadyawesome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class LoadActivity : AppCompatActivity() {
    var shopList: MutableList<Shop>? = mutableListOf<Shop>()
    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        // Firebase에서 shop 데이터를 가져옴
        getShopData()

        // Firebase에서 member 데이터를 가져옴
        getMemberData()

        // 로딩 화면이 충분한 시간 출력될 수 있도록 4초 동안 딜레이를 건다.
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            // Firebase에서 shop 데이터를 가져올 때 사용했던 count 값을 intent로 LoginActivity에 전달
            intent.putExtra("count", count)
            startActivity(intent)
            finish()
        }, 4000)
    }

    // Firebase로부터 shop을 가져오는 함수
    fun getShopData() {
        val shopDAOShop = ShopDAO()
        shopDAOShop.selectShop()?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 데이터를 받을 shopList를 초기화
                shopList?.clear()
                // 향상된 for문으로 데이터를 하나씩 전달 받음
                for (data in snapshot.children) {
                    val shopData = data.getValue(Shop::class.java)
                    if (shopData != null) {
                        // shopList에 shopData를 저장
                        // 저장할 때마다 count에 1씩 더함
                        shopList?.add(shopData)
                        count += 1
                        Log.d("kr.or.mrhi", "LoadActivity selectShop()(Firebase) onDataChange")
                    }
                }
                Log.d("kr.or.mrhi", "getShopData() 종료")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("kr.or.mrhi", "LoadActivity selectShop() onCancelled")
            }
        })
    }

    // Firebase로부터 member 데이터를 가져오는 함수
    fun getMemberData() {
        val dbHelper = DBHelper(this, MainActivity.DB_NAME, MainActivity.VERSION)
        // Firebase로부터 데이터를 받기 전에, 데이터를 저장할 SQLite를 초기화시킴
        val deleteFlag = dbHelper.deleteMemberAll()
        // 초기화가 완료되면 Firebase로부터 데이터를 받음
        if (deleteFlag) {
            val shopDAOMember = ShopDAO()
            shopDAOMember.selectMember()?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val member = data.getValue(Member::class.java)
                        if (member != null) {
                            // 데이터베이스에 데이터를 저장
                            // 굳이 여기서 저장하는 이유는 다음 Activity인 LoginActivity에서 사용해야하기 때문
                            dbHelper.insertMember(member)
                            Log.d("kr.or.mrhi", "LoadActivity selectMember(Firebase) onDataChange")
                        }
                    }
                    Log.d("kr.or.mrhi", "getMemberData() 종료")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("kr.or.mrhi", "LoadActivity selectMember(Firebase) onCancelled")
                }
            })
        }
    }
}