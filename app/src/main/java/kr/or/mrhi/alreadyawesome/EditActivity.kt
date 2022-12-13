package kr.or.mrhi.alreadyawesome

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import kr.or.mrhi.alreadyawesome.*
import kr.or.mrhi.alreadyawesome.databinding.ActivityEditBinding
import java.time.LocalDateTime

class EditActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditBinding
    lateinit var key: String
    lateinit var id: String
    lateinit var password: String
    lateinit var passwordCheck: String
    lateinit var name: String
    lateinit var gender: String
    lateinit var phone: String
    lateinit var email: String
    lateinit var rate: String
    var year: String = ""
    var month: String = ""
    var day: String = ""
    var passwordFlag = false
    var passwordCheckFlag = false
    var nameFlag = false
    var yearFlag = false
    var monthFlag = false
    var dayFlag = false
    var phoneFlag = false
    var emailFlag = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SQLite로부터 userId를 불러와서 이를 memberList -> member 순으로 찾아감
        val dbHelper = DBHelper(this, MainActivity.DB_NAME, MainActivity.VERSION)
        val userId = dbHelper.selectUser()
        val memberList = dbHelper.selectMemberById(userId)
        val member = memberList?.get(0)

        // member에 저장된 값을 저장
        key = member?.memberKey.toString()
        id = member?.memberId.toString()
        password = member?.password.toString()
        name = member?.memberName.toString()
        // birthDate를 수정하기 위해서 year, month, day값으로 분리함
        year = member?.birthDate?.substring(0, 4).toString()
        month = member?.birthDate?.substring(6, 8).toString()
        day = member?.birthDate?.substring(10, 12).toString()
        gender = member?.gender.toString()
        phone = member?.memberPhone.toString()
        email = member?.email.toString()
        rate = member?.rate.toString()

        // TextView에 상기에서 정의한 값을 입력
        binding.tvEditId.text = id
        binding.edtEditPassword.setText(password)
        binding.edtEditName.setText(name)
        binding.edtEditBirthYear.setText(year)
        binding.edtEditBirthMonth.setText(month)
        binding.edtEditBirthDay.setText(day)
        if (gender == "남성") {
            binding.rbMale.isChecked = true
        } else {
            binding.rbFemale.isChecked = true
        }
        binding.edtEditPhone.setText(phone)
        binding.edtEditEmail.setText(email)

        binding.rgGender.setOnCheckedChangeListener { radioGroup, select ->
            when(select){
                binding.rbMale.id -> {
                    gender = "남성"
                }
                binding.rbFemale.id ->{
                    gender = "여성"
                }
            }
        }

        // Edit 버튼을 누르면 실행
        // EditView에 적힌 내용이 member의 내용과 다른 경우에 한해서 패턴 검색을 진행
        binding.btnEditEdit.setOnClickListener {
            if (binding.edtEditPassword.text.toString() != password) {
                checkPassword()
            } else if (binding.edtEditName.text.toString() != name) {
                checkName()
            } else if (binding.edtEditBirthYear.text.toString() != year) {
                checkBirthYear()
            } else if (binding.edtEditBirthMonth.text.toString() != month) {
                checkBirthMonth()
            } else if (binding.edtEditBirthDay.text.toString() != day) {
                checkBirthDay()
            } else if (binding.edtEditPhone.text.toString() != phone) {
                checkPhone()
            } else if (binding.edtEditEmail.text.toString() != email) {
                checkEmail()
            } else if (binding.edtEditPasswordCheck.text.toString() != password) {
                checkPasswordCheck()
            // 모든 패턴이 맞을 경우,
            } else {
                // 상기에서 year, month, day로 나눴던 생일을 다시 합침
                val birthDate = "${year}년 ${month}월 ${day}일"
                // key값을 기준으로 데이터를 찾아서 Firebase에 저장된 내용을 수정함
                val shopDAO = ShopDAO()
                val hashMap : HashMap<String, Any> = HashMap()
                hashMap["memberKey"] = key
                hashMap["memberId"] = id
                hashMap["password"] = password
                hashMap["memberName"] = name
                hashMap["birthDate"] = birthDate
                hashMap["gender"] = gender
                hashMap["memberPhone"] = phone
                hashMap["email"] = email
                hashMap["rate"] = rate
                shopDAO.updateMember(key, hashMap).addOnSuccessListener {
                    Log.d("kr.or.mrhi","EditActivity updateMember(Firebase) OnSuccess")
                }.addOnFailureListener {
                    Log.d("kr.or.mrhi", "EditActivity updateMember(Firebase) OnFailure")
                }
            backToMainActivty("SUCCESS")
            }
        }

        // Cancel 버튼을 누르면 MainActivity로 이동
        binding.btnEditCancel.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // 비밀번호 패턴을 검사
    fun checkPassword() {
        passwordFlag = false
        password = binding.edtEditPassword.text.toString()
        if (password.isNotEmpty() && !password.matches("^[A-Za-z0-9]{6,20}$".toRegex())) {
            Toast.makeText(this, "비밀번호는 영문과 숫자를 6자리 이상 설정 해 주세요", Toast.LENGTH_SHORT).show()
            binding.edtEditPassword.setText(password)
        } else if(password.isNotEmpty() && password.matches("^[A-Za-z0-9]{6,20}$".toRegex())) {
            passwordFlag = true
        }
    }

    // 이름 패턴을 검사
    fun checkName() {
        nameFlag = false
        name = binding.edtEditName.text.toString()
        if (name.isNotEmpty() && !name.matches("^[가-힣]{2,5}$".toRegex())) {
            Toast.makeText(this,"이름은 한글이름으로 2~5자리 이내 입력해 주세요", Toast.LENGTH_SHORT).show()
            binding.edtEditName.setText(name)
        } else if(name.isNotEmpty() && name.matches("^[가-힣]{2,5}$".toRegex())) {
            nameFlag = true
        }
    }

    // 탄생년 패턴을 검사
    @RequiresApi(Build.VERSION_CODES.O)
    fun checkBirthYear() {
        yearFlag = false
        year = binding.edtEditBirthYear.text.toString()
        if (year != "") {
            val yearInt = year.toInt()
            var yearIntFlag = false
            val today = LocalDateTime.now()
            val thisYear = today.year
            if (yearInt in 1900..thisYear) {
                yearIntFlag = true
            }
            if (year.isNotEmpty() && !yearIntFlag) {
                Toast.makeText(this,"정확한 탄생년을 입력해 주세요", Toast.LENGTH_SHORT).show()
                binding.edtEditBirthYear.setText(year)
            } else if(year.isNotEmpty() && yearIntFlag) {
                yearFlag = true
            }
        }
    }

    // 탄생월 패턴을 검사
    fun checkBirthMonth() {
        monthFlag = false
        month = binding.edtEditBirthMonth.text.toString()
        if (month != "") {
            val monthInt = month.toInt()
            var monthIntFlag = false
            if (monthInt in 1..12) {
                monthIntFlag = true
            }
            if (!monthIntFlag && month.isNotEmpty()) {
                Toast.makeText(this, "정확한 탄생월을 입력해 주세요", Toast.LENGTH_SHORT).show()
                binding.edtEditBirthMonth.setText(month)
            } else if (monthIntFlag && month.isNotEmpty()) {
                monthFlag = true
            }
        }
        if (month.length == 1) {
            month = "0$month"
        }
    }

    // 탄생일 패턴을 검사
    fun checkBirthDay() {
        dayFlag = false
        day = binding.edtEditBirthDay.text.toString()
        if(day != "") {
            val monthInt = month.toInt()
            val dayInt = day.toInt()
            val dayIntFlag = checkBirthDate(monthInt, dayInt)
            if (!dayIntFlag && day.isNotEmpty()) {
                Toast.makeText(this, "정확한 탄생일을 입력해 주세요", Toast.LENGTH_SHORT).show()
                binding.edtEditBirthDay.setText(day)
            } else if(dayIntFlag && day.isNotEmpty()){
                dayFlag = true
            }
        }
        if (day.length == 1) {
            day = "0$day"
        }
    }

    // 월에 따른 날짜 범위를 검사
    fun checkBirthDate(monthInt: Int, dayInt:Int) : Boolean {
        var returnValue = false
        when(monthInt) {
            1 -> returnValue = checkBirthDateByMonth(3, dayInt)
            2 -> returnValue = checkBirthDateByMonth(1, dayInt)
            3 -> returnValue = checkBirthDateByMonth(3, dayInt)
            4 -> returnValue = checkBirthDateByMonth(2, dayInt)
            5 -> returnValue = checkBirthDateByMonth(3, dayInt)
            6 -> returnValue = checkBirthDateByMonth(2, dayInt)
            7 -> returnValue = checkBirthDateByMonth(3, dayInt)
            8 -> returnValue = checkBirthDateByMonth(3, dayInt)
            9 -> returnValue = checkBirthDateByMonth(2, dayInt)
            10 -> returnValue = checkBirthDateByMonth(3, dayInt)
            11 -> returnValue = checkBirthDateByMonth(2, dayInt)
            12 -> returnValue = checkBirthDateByMonth(3, dayInt)
        }
        return returnValue
    }

    fun checkBirthDateByMonth(type: Int, dayInt: Int) : Boolean {
        var returnValue = false
        when(type) {
            1 -> if(dayInt in 1 .. 28) {
                returnValue = true
            }
            2 -> if(dayInt in 1..30) {
                returnValue =  true
            }
            3 -> if(dayInt in 1 .. 31) {
                returnValue = true
            }
        }
        return returnValue
    }

    // 전화번호 패턴을 검사
    fun checkPhone() {
        phoneFlag = false
        phone = binding.edtEditPhone.text.toString()
        if (phone.isNotEmpty() && !phone.matches("^010[0-9]{4}[0-9]{4}$".toRegex())) {
            Toast.makeText(this, "- 없이 번호를 정확하게 입력해 주세요", Toast.LENGTH_SHORT).show()
            binding.edtEditPhone.setText(phone)
        } else if(phone.isNotEmpty() && phone.matches("^010[0-9]{4}[0-9]{4}$".toRegex())) {
            phoneFlag = true
        }
    }

    // 이메일 패턴을 검사
    fun checkEmail() {
        emailFlag = false
        email = binding.edtEditEmail.text.toString()
        if (email.isNotEmpty() && !email.contains("@") && !(email.length > 10)){
            Toast.makeText(this,"이메일을 정확하게 입력해 주세요", Toast.LENGTH_SHORT).show()
            binding.edtEditEmail.setText(email)
        } else if (email.isNotEmpty() && email.contains("@") && email.length > 10) {
            emailFlag = true
        }
    }

    // 비밀번호(확인) 패턴을 검사
    fun checkPasswordCheck() {
        passwordCheckFlag = false
        passwordCheck = binding.edtEditPasswordCheck.text.toString()
        if (binding.edtEditPassword.text.toString() != password) {
            Toast.makeText(this, "먼저 비밀번호를 입력해 주세요", Toast.LENGTH_SHORT).show()
        } else if (passwordCheck.isEmpty()) {
            Toast.makeText(this, "비밀번호 확인란에 비밀번호를 한 번 더 입력해주세요", Toast.LENGTH_SHORT).show()
        } else if (passwordCheck.isNotEmpty() && password != passwordCheck) {
            Toast.makeText(this, "비밀번호가 다릅니다. 확인 후 다시 입력해 주세요", Toast.LENGTH_SHORT).show()
            binding.edtEditPasswordCheck.text.clear()
        } else if (passwordCheck.isNotEmpty() && password == passwordCheck) {
            passwordCheckFlag = true
        }
    }

    // MainActivity로 이동하는 함수
    // 각 위치로부터 value 값을 받아서 intent로 전달함 (CANCEL, SUCCESS)
    fun backToMainActivty(value: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("editUserData", value)
        startActivity(intent)
        finish()
    }

    // 백버튼을 누르면 회원정보 수정을 취소하겠는지 묻는 다이얼로그창을 출력
    override fun onBackPressed() {
        val eventHandler = object: DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, answer: Int) {
                if(answer == DialogInterface.BUTTON_POSITIVE){
                    backToMainActivty("CANCEL")
                }
            }
        }
        AlertDialog.Builder(this).run {
            setMessage("회원정보 수정을 취소하시겠습니까?")
            setPositiveButton("네", eventHandler)
            setNegativeButton("아니요", null)
            show()
        }
    }
}