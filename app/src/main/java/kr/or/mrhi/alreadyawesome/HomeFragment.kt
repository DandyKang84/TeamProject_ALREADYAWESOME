package kr.or.mrhi.alreadyawesome

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kr.or.mrhi.alreadyawesome.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    lateinit var mainContext: Context
    lateinit var homeAdapter: HomeAdapter
    var shopHairList: MutableList<Shop>? = mutableListOf<Shop>()
    var shopNailList: MutableList<Shop>? = mutableListOf<Shop>()
    var shopMakeUpList: MutableList<Shop>? = mutableListOf<Shop>()
    var shopSkinCareList: MutableList<Shop>? = mutableListOf<Shop>()

    // onAttach를 통해서 MainActivity의 context를 불러옴
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 파이어베이스로부터 shopList를 가져옴
        getShopList()

        // 버튼을 눌렸을 때 해당 액티비티로 이동
        buttonClick(binding.btnHomeHair, HairActivity())
        buttonClick(binding.btnHomeNail, NailActivity())
        buttonClick(binding.btnHomeMakeUp, MakeUpActivity())
        buttonClick(binding.btnHomeSkinCare, SkinCareActivity())

        return binding.root
    }

    // 중복되는 내용을 줄이기 위해서 Intent 부분을 함수로 만들어서 사용
    fun buttonClick(selectButton: Button, activity: Activity) {
        selectButton.setOnClickListener {
            val intent = Intent(mainContext, activity::class.java)
            startActivity(intent)
            // Intent로 다른 Activity로 넘어가면, MainActivity는 종료
            (mainContext as MainActivity).finish()
        }
    }

    // 파이어베이스로부터 shopData를 가져오기 위한 함수
    fun getShopList() {
        val dbHelper = DBHelper(mainContext, MainActivity.DB_NAME, MainActivity.VERSION)
        val deleteFlag = dbHelper.deleteShopAll()
        if (deleteFlag) {
            val shopDAO = ShopDAO()
            shopDAO.selectShop()?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // 먼저 데이터를 담을 List들을 초기화
                    shopHairList?.clear()
                    shopNailList?.clear()
                    shopMakeUpList?.clear()
                    shopSkinCareList?.clear()
                    for (data in snapshot.children) {
                        val shop = data.getValue(Shop::class.java)
                        shop?.shopId = data.key.toString()
                        if (shop != null) {
                            // 타입에 따라서 데이터를 나눠서 담음
                            val insertFlag = dbHelper.insertShop(shop)
                            if(insertFlag) {
                                shopTypeCheck(shop)
                            }
                        }
                    }
                    Log.d("kr.or.mrhi", "HomeFragment selectShop() onDataChange")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("kr.or.mrhi", "HomeFragment selectShop() onCancelled")
                }
            })
        }
    }

    // 매장 타입에 따라서 데이터를 나눠서 담기 위함
    fun shopTypeCheck(shop: Shop) {
        when(shop.type) {
            HairActivity.HAIR_KO -> {
                // List에 데이터를 넣고, recyclerView에 Adpater을 연결함
                createRecyclerView(shopHairList, binding.rvHomeHair, shop)
            }
            NailActivity.NAIL_KO -> {
                createRecyclerView(shopNailList, binding.rvHomeNail, shop)
            }
            MakeUpActivity.MAKEUP_KO -> {
                createRecyclerView(shopMakeUpList, binding.rvHomeMakeUp, shop)
            }
            SkinCareActivity.SKINCARE_KO -> {
                createRecyclerView(shopSkinCareList, binding.rvHomeSkinCare, shop)
            }
        }
    }

    // 중복되는 코드를 생략하기 위하여 함수를 생성
    // 해당 함수 내에서 List에 데이터를 넣고, recyclerView에 Adpater을 연결함
    fun createRecyclerView(shopList: MutableList<Shop>?, recyclerView: RecyclerView, shop: Shop) {
        // 데이터들을 shopHairList에 담고,
        shopList?.add(shop)
        // shopHairList를 shopGrade라는 기준에 맞춰서 내림정렬 함
        shopList?.sortByDescending { it.shopGrade }
        // 데이터가 가로로 배열되도록 LinearLayoutManager를 설정
        val linearLayoutManager = LinearLayoutManager(mainContext, LinearLayoutManager.HORIZONTAL, false)
        // HomeAdapter에 contextd와 shopHairList를 입력
        homeAdapter = HomeAdapter(mainContext, shopList)
        // RecyclerView에 상기에서 정리한 linearLayoutManger와 HomeAdapter를 연결
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = homeAdapter
        // 입력된 데이터가 변경(추가)되었을음 알리기 위해서 notifyDataSetChanged를 선언
        homeAdapter.notifyDataSetChanged()
    }
}