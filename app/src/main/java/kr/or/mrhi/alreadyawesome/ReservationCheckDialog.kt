package kr.or.mrhi.alreadyawesome

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kr.or.mrhi.alreadyawesome.databinding.DialogReservationCheckBinding

class ReservationCheckDialog (val context : Context){
    var reservationList: MutableList<Reservation> = mutableListOf()
    var memberList: MutableList<Member>? = mutableListOf()
    lateinit var member: Member
    val dialog = Dialog(context)
    var userId = ""
    var total = 0
    var rate = ""

    fun showDialog(reservation: Reservation) {
        // 어느 화면을 출력할 건지 선택
        val binding = DialogReservationCheckBinding.inflate(LayoutInflater.from(context))
        // 선택한 화면을 셋팅
        dialog.setContentView(binding.root)
        // 화면 크기를 설정
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        // 백버튼을 허가할 건지 선택(불허가)
        dialog.setCanceledOnTouchOutside(true)
        // 화면 외부를 눌렀을 때 화면이 꺼지게 할 건지(불허가)
        dialog.setCancelable(true)
        // 다이얼로그창 출력
        dialog.show()
        val dbHelper = DBHelper(context, MainActivity.DB_NAME, MainActivity.VERSION)
        // calculateRate()에서 사용할 member를 위해서 userId -> memberList -> member로 데이터를 가져옴
        userId = reservation.memberId
        memberList = dbHelper.selectMemberById(userId)
        member = memberList!![0]

        // TextView에 ReservationDialog에서 입력한 내용을 출력
        binding.tvShowDate.text = reservation.reserveDate
        binding.tvShowTime.text = reservation.reserveTime
        binding.tvShowMenu.text = reservation.reserveMenu
        binding.tvShowPayment.text = reservation.payment
        binding.tvShowPrice.text = "${reservation.price}원"

        // OK버튼을 누르면 Firebase에 데이터가 들어감
        binding.btnReservationCheckOk.setOnClickListener {
            val insetShopDAO = ShopDAO()
            // Firebase에 데이터를 넣는 척 하면서 key값을 받음
            val reserveKey = insetShopDAO.reservationDatabaseReference?.push()?.key
            val reserveShopId = reservation.shopId
            val reserveMemberId = userId
            val reserveDate = reservation.reserveDate
            val reserveTime = reservation.reserveTime
            val reserveMenu = reservation.reserveMenu
            val reservePrice = reservation.price
            val reservePayment = reservation.payment
            val newReservation = Reservation(reserveKey, reserveShopId, reserveMemberId, reserveDate, reserveTime,
                                reserveMenu, reservePrice, reservePayment)
            // 상기에서 받은 key값에 데이터를 넣음
            insetShopDAO.reservationDatabaseReference?.child(reserveKey!!)?.setValue(newReservation)
                    ?.addOnSuccessListener {
                        Toast.makeText(context, "예약이 완료 되었습니다\n감사합니다", Toast.LENGTH_SHORT).show()
                        Log.d("kr.or.mrhi", "ReservationCheckDialog insertReservation OnSuccess")
                    }?.addOnFailureListener {
                        Log.d("kr.or.mrhi", "ReservationCheckDialog insertReservation OnFailure")
                    }

            // 회원 등급을 계산함
            calculateRate()
            dialog.dismiss()
        }

        // Close 버튼을 누르면 다이얼로그창이 닫힘
        binding.btnReservationCheckClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    // 회원 등급을 계산하기 위한 함수
    fun calculateRate() {
        val selectShopDAO = ShopDAO()
        // Firebase로부터 reservaiton 값을 가져옴
        selectShopDAO.selectReservation()?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val reservation = data.getValue(Reservation::class.java)
                    if (reservation != null) {
                        // reservation의 memberId가 로그인한 userId와 일치하는 경우에 한해서 해당 데이터를 저장함
                        if(reservation.memberId == userId) {
                            reservationList.add(reservation)
                        }
                    }
                }
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

            override fun onCancelled(error: DatabaseError) {
                Log.d("kr.or.mrhi", "ReservationListActivity selectReview() onCancelled")
            }
        })
    }
}