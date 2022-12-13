package kr.or.mrhi.alreadyawesome

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import kr.or.mrhi.alreadyawesome.databinding.DialogReservationBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class ReservationDialog(val context : Context) {
    lateinit var binding: DialogReservationBinding
    @RequiresApi(Build.VERSION_CODES.O)
    val today: LocalDate = LocalDate.now()
    @RequiresApi(Build.VERSION_CODES.O)
    val now: LocalTime = LocalTime.now()
    val dialog = Dialog(context)
    var selectDate: String = ""
    var selectTime :String = ""
    var selectMenu :String = ""
    var selectPay : String = ""
    var selectPrice : String = ""
    var date = ""
    var time = ""
    var year = 0
    var month = 0
    var day = 0
    var hour = 0
    var minute = 0
    var menuFlag = false
    var dateFlag = false
    var timeFlag = false
    var paymentFlag = false

    @RequiresApi(Build.VERSION_CODES.O)
    fun showDialog(shop: Shop) {
        binding = DialogReservationBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()

        // 매장 타입에 따라서 선택 가능한 메뉴 내용을 달리함
        when(shop.type) {
            HairActivity.HAIR_KO -> {
                setMenu("커트", "파마", "염색")
            }
            NailActivity.NAIL_KO -> {
                setMenu("네일", "패디", "케어")
            }
            MakeUpActivity.MAKEUP_KO -> {
                setMenu("원장님", "수석실장님", "디자이너")
            }
            SkinCareActivity.SKINCARE_KO -> {
                setMenu("코스1", "코스2", "코스3")
            }
        }

        // 선택한 메뉴에 따라서 menu와 이와 연결된 price값을 지정함
        binding.rgMenu.setOnCheckedChangeListener { radioGroup, menu ->
            when(menu){
                binding.rbtnReserveMenu1.id ->{
                    selectMenu = binding.rbtnReserveMenu1.text.toString()
                    selectPrice = shop.price1
                    menuFlag = true
                }
                binding.rbtnReserveMenu2.id ->{
                    selectMenu = binding.rbtnReserveMenu2.text.toString()
                    selectPrice = shop.price2
                    menuFlag = true
                }
                binding.rbtnReserveMenu3.id ->{
                    selectMenu = binding.rbtnReserveMenu3.text.toString()
                    selectPrice = shop.price3
                    menuFlag = true
                }
            }
        }

        // 예약 날짜를 선택함
        // 예약 날짜를 선택한 경우에 한하여 dateFlag를 true로 설정함(선택하지 않으면 false)
        binding.dpReservationChooseDate.setOnDateChangedListener { datePicker, y, m, d ->
            year = y
            month = m + 1
            val selectMonth = checkIt(month)
            day = d
            val selectDay = checkIt(day)

            selectDate = "${year}년 ${selectMonth}월 ${selectDay}일"
            dateFlag = true
        }

        // 예약 시간을 선택하는데, default값을 12:00 pm으로 지정함
        // 예약 시간을 선택한 경우에 한하여 timeFlag true로 설정함(선택하지 않으면 false)
        binding.tpReservationChooseTime.hour = 12
        binding.tpReservationChooseTime.minute = 0
        binding.tpReservationChooseTime.setOnTimeChangedListener { timePicker, h, m ->
            hour = h
            minute = m
            val selectMinute = checkIt(minute)
            selectTime = "${hour}시 ${selectMinute}분"
            timeFlag = true
        }

        // 결제 방법을 선택함
        binding.rgReservationChoosePayment.setOnCheckedChangeListener { radioGroup, payment ->
            when(payment){
                binding.rbReservationCard.id ->{
                    selectPay = binding.rbReservationCard.text.toString()
                    paymentFlag = true
                }
                binding.rbReservationCash.id ->{
                    selectPay = binding.rbReservationCash.text.toString()
                    paymentFlag = true
                }
            }
        }

        // OK 버튼을 누른 경우 dateFlag와 timeFlag의 true/false에 따라서 예약한 날짜와 시간을 입력함
        binding.btnReservationOk.setOnClickListener {
            val checkDialog = ReservationCheckDialog(context)
            date = selectDate
            time = selectTime
            // Datepicker를 조작하지 않은 경우에는 날짜가 오늘 날짜로 들어감
            if (!dateFlag){
                date = today.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))
                year = today.year
                month = today.monthValue
                day = today.dayOfMonth
            }
            // TimePicker를 조작하지 않은 경우에는 시간이 12시로 들어감
            if (!timeFlag) {
                time = "12시 00분"
                hour = 12
                minute = 0
            }

            // dateFlag와 timeFlag를 통해서 입력된 값이 올바른 값인지를 checkFlag로 확인
            val checkFlag = checkDateTime(shop)
            // 만약 메뉴와 결제방법을 선택하지 않은 경우에는 다음으로 넘어가지 못하게 하고 토스트 메시지 출력
            if (!menuFlag) {
                Toast.makeText(context, "메뉴를 선택해주세요", Toast.LENGTH_SHORT).show()
            } else if (!paymentFlag) {
                Toast.makeText(context, "결제 방법을 선택해주세요", Toast.LENGTH_SHORT).show()
            // 별다른 이상이 없는 경우에는 ReservationCheckDialog로 이동
            } else if (checkFlag) {
                val dbHelper = DBHelper(context, MainActivity.DB_NAME, MainActivity.VERSION)
                val menu = selectMenu
                val shopId = shop.shopId
                val memberId = dbHelper.selectUser()
                val price = selectPrice
                val payment = selectPay
                val reservation = Reservation("", shopId, memberId, date, time, menu, price, payment)
                checkDialog.showDialog(reservation)
                dialog.dismiss()
            }
        }

        // Close 버튼을 누르면 다이얼로그창이 닫힘
        binding.btnReservationClose.setOnClickListener {
            dialog.dismiss()
        }
    }

    // 반복되는 코드를 줄이기 위해 사용한 함수
    fun setMenu(menu1: String, menu2: String, menu3: String) {
        binding.rbtnReserveMenu1.text = menu1
        binding.rbtnReserveMenu2.text = menu2
        binding.rbtnReserveMenu3.text = menu3
    }

    // 날짜와 시간이 올바르게 입력되었는지를 판단하기 위해 사용한 함수
    @RequiresApi(Build.VERSION_CODES.O)
    fun checkDateTime(shop: Shop) : Boolean {
        var timeCheckFlag = false
        var checkFlag = false

        // 10보다 작은 숫자는 앞에 0을 붙여서 date 또는 LocalDate 또는 LocalTime으로 변환할 수 있게 함
        val checkMonth = checkIt(month)
        val checkDay = checkIt(day)
        val checkHour = checkIt(hour)
        val checkMinute = checkIt(minute)

        // 날짜가 입력된 String 값과 String이 어떻게 구성되었는지가 적혀있는 DateTimeFormatter를 전달해서 String를 LocalDate로 변환
        val checkSelectDate = "${year}년 ${checkMonth}월 ${checkDay}일"
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        val dateDate = LocalDate.parse(checkSelectDate, dateFormatter)

        // 변환된 date 값이 오늘로부터 50일 이내의 날짜인지를 확인 후 맞거나 아니거나에 따라서 토스트 메시지 출력
        val dateCheckFlag = if(dateDate > today.plusDays(50)) {
            Toast.makeText(context, "해당 일자는 아직 예약을 받고 있지 않습니다\n일자를 다시 선택해주세요", Toast.LENGTH_SHORT).show()
            false
        // 또한 오늘 이전 날짜를 선택한 경우에도 토스므 메시지 출력
        } else if(dateDate < today) {
            Toast.makeText(context, "오늘 이전 날짜를 선택하셨습니다\n일자를 다시 선택해주세요", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }

        // date에 문제가 없을 경우에는 이번에는 time을 확인
        if (dateCheckFlag) {
            // date때와 동일하게 시간이 입력된 String 값과 String이 어떻게 구성되었는지가 적혀있는 DateTimeFormatter를 전달해서 String를 LocalTime으로 변환
            // 시간의 경우에는 가게의 개점시간과 폐점시간도 함께 변환해서 계산을 보다 용이하게 하고자 함
            val checkSelectTime = "${checkHour}시 ${checkMinute}분"
            val timeFormatter = DateTimeFormatter.ofPattern("HH시 mm분")
            val shopTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val timeTime = LocalTime.parse(checkSelectTime, timeFormatter)
            val openTime = LocalTime.parse(shop.openTime, shopTimeFormatter)
            val closeTime = LocalTime.parse(shop.closeTime, shopTimeFormatter)
            // 선택한 시간이 개점시간 이전 시간이거나, 폐점시간 이후 시간일 경우에는 토스트 메시지 출력
            timeCheckFlag = if (timeTime < openTime || timeTime > closeTime) {
                Toast.makeText(context, "영업시간이 아닙니다\n시간을 다시 선택해주세요", Toast.LENGTH_SHORT).show()
                false
            // 또한 선택한 날짜가 이미 지나간 날짜일 경우에는 마찬가지로 토스트 메시지 출력
            } else if(dateDate == today && timeTime < now) {
                Toast.makeText(context, "이미 지난 시간입니다\n시간을 다시 선택해주세요", Toast.LENGTH_SHORT).show()
                false
            } else {
                true
            }
        }

        // date와 time이 모두 통과한 경우에 한하여 checkFlag = true
        if(dateCheckFlag && timeCheckFlag) checkFlag = true

        return checkFlag
    }

    // String 값을 date 또는 time으로 변경하기 위해서 10보다 작은 숫자 앞에 0을 붙임
    fun checkIt(checkValue: Int) : String {
        val returnValue = if(checkValue < 10) {
            "0$checkValue"
        } else {
            "$checkValue"
        }
        return returnValue
    }
}