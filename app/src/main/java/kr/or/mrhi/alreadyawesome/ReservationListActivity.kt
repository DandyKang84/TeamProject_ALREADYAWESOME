package kr.or.mrhi.alreadyawesome

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kr.or.mrhi.alreadyawesome.databinding.ActivityReservationListBinding

class ReservationListActivity : AppCompatActivity() {
    lateinit var binding: ActivityReservationListBinding
    lateinit var reservationListAdapter: ReservationListAdapter
    var reservationList: MutableList<Reservation> = mutableListOf<Reservation>()
    var userId = ""
    var total = 0
    var rate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase에서 reservation을 가져옴
        insertReservation()
    }

    // Firebase에서 reservation 데이터를 가져옴
    fun insertReservation() {
        val dbHelper = DBHelper(this, MainActivity.DB_NAME, MainActivity.VERSION)
        // SQLite에서 userId를 받아옴
        userId = dbHelper.selectUser()
        val shopDAO = ShopDAO()
        shopDAO.selectReservation()?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val reservation = data.getValue(Reservation::class.java)
                    if (reservation != null) {
                        // reservation 속 memberId가 userId와 일치하는 데이터만 저장함
                        if (reservation.memberId == userId) {
                            reservationList.add(0, reservation)
                        }
                    }
                }
                // 위에서 저장한 reservationList를 적용한 Adapter와 LinearLayoutManager와 recyclerView를 연결함
                reservationListAdapter = ReservationListAdapter(this@ReservationListActivity, reservationList)
                binding.rvReservationList.adapter = reservationListAdapter
                binding.rvReservationList.layoutManager = LinearLayoutManager(this@ReservationListActivity)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("kr.or.mrhi", "ReservationListActivity selectReview() onCancelled")
            }
        })
    }

    // 백버튼을 누르면 MainActivity로 이동
    // from은 MainActivity의 MyFragment로부터 이동해왔고, 그 위치로 돌아간다는 의미
    override fun onBackPressed() {
        calculateRate()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("from", MainActivity.TAB_MY)
        startActivity(intent)
        finish()
    }

    // 액티비티를 종료할 때 회원 등급을 계산하기 위한 함수
    fun calculateRate() {
        val dbHelper = DBHelper(this, MainActivity.DB_NAME, MainActivity.VERSION)
        val memberList = dbHelper.selectMemberById(userId)
        val member = memberList!![0]
        // reservation의 price를 모두 가져와서 total에 저장 후, 이를 기준으로 등급을 나눔
        for (position in 0 until reservationList.size) {
            // String 값을 저장된 price값을 더하기 위해서는 int값으로 변경해야하고, 그 전에 ,를 제거함
            val priceString = reservationList[position].price.replace(",", "")
            // price를 int값을 변환 후 total에 더함
            val price = priceString.toInt()
            total += price
        }
        // 총 이용금액에 따라서 등급을 나눔
        rate = if (total in 500_000 until 1_000_000) {
            RegisterActivity.silver
        } else if (total > 1_000_000) {
            RegisterActivity.gold
        } else {
            RegisterActivity.bronze
        }
        // 수정된 rate 값으로 Firebase에 저장된 member 정보를 수정함
        Log.d("kr.or.mrhi", "total $total  rate $rate")
        val shopDAO = ShopDAO()
        val hashMap : HashMap<String, Any> = HashMap()
        val key = member.memberKey.toString()
        hashMap["memberKey"] = key
        hashMap["memberId"] = member.memberId
        hashMap["password"] = member.password
        hashMap["memberName"] = member.memberName
        hashMap["birthDate"] = member.birthDate
        hashMap["gender"] = member.gender
        hashMap["memberPhone"] = member.memberPhone
        hashMap["email"] = member.email
        hashMap["rate"] = rate
        shopDAO.updateMember(key, hashMap).addOnSuccessListener {
            Log.d("kr.or.mrhi","${member.memberId}님의 등급이 성공적으로 수정되었습니다. $rate")
        }.addOnFailureListener {
            Log.d("kr.or.mrhi", "Firebase Member Update Failure")
        }
    }

    // ReservationListAdapter에서 reservation을 삭제할 때 사용
    fun removeReservation(position: Int, reservation: Reservation?) {
        val shopDAO = ShopDAO()
        val reserveKey = reservation?.reserveKey.toString()

        shopDAO.deleteShop(reserveKey).addOnSuccessListener {
            Toast.makeText(this,"${reservation?.reserveDate} 예약이 취소 되었습니다.", Toast.LENGTH_SHORT).show()
            reservationList.removeAt(position)
            reservationListAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this,"예약 취소를 다시 진행해 주세요.", Toast.LENGTH_SHORT).show()
        }
    }
}