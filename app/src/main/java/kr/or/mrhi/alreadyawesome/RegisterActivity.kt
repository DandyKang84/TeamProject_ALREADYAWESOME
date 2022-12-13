package kr.or.mrhi.alreadyawesome

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import kr.or.mrhi.alreadyawesome.databinding.ActivityRegisterBinding
import java.time.LocalDateTime

class RegisterActivity : AppCompatActivity() {
    companion object {
        const val gold = "gold"
        const val silver = "silver"
        const val bronze = "bronze"
    }
    lateinit var binding: ActivityRegisterBinding
    lateinit var id: String
    lateinit var password: String
    lateinit var passwordCheck: String
    lateinit var name: String
    lateinit var phone: String
    lateinit var email: String
    lateinit var gender: String
    lateinit var year: String
    lateinit var month: String
    lateinit var day: String
    var idFlag = false
    var passwordFlag = false
    var passwordCheckFlag = false
    var nameFlag = false
    var yearFlag = false
    var monthFlag = false
    var dayFlag = false
    var genderFlag = false
    var phoneFlag = false
    var emailFlag = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 아이디 체크 버튼을 클릭하면,
        binding.btnRegisterIdCheck.setOnClickListener {
            // idFlag의 디폴트 값을 생성
            idFlag = false
            id = binding.edtRegisterId.text.toString()
            val dbHelper = DBHelper(this, MainActivity.DB_NAME, MainActivity.VERSION)
            // SQLitie에 해당 아이디가 있는 확인
            if (dbHelper.selectMemberCheckId(id)) {
                Toast.makeText(this, "중복된 아이디 입니다.", Toast.LENGTH_SHORT).show()
                binding.edtRegisterId.text.clear()
            // 패턴 검색
            } else if (!id.matches("^[a-zA-Z0-9]{6,20}$".toRegex())) {
                Toast.makeText(this, "아이디는 영문과 숫자를 입력해 6자리 이상 사용해 주시기 바랍니다.", Toast.LENGTH_SHORT)
                    .show()
                binding.edtRegisterId.text.clear()
            // 문제가 없을 경우 통과
            } else if (!dbHelper.selectMemberCheckId(id) && id.matches("^[a-zA-Z0-9]{6,20}$".toRegex())) {
                Toast.makeText(this, "사용가능한 아이디 입니다.", Toast.LENGTH_SHORT).show()
                idFlag = true
            }
        }

        // 비밀번호를 입력한 후에 다른 곳을 클릭하면
        binding.edtRegisterPassword.setOnFocusChangeListener { view, hasFocus ->
            passwordFlag = false
            // 패턴 검색
            password = binding.edtRegisterPassword.text.toString()
            if (!hasFocus && password.isNotEmpty() && !password.matches("^[A-Za-z0-9]{6,20}$".toRegex())) {
                Toast.makeText(this, "비밀번호는 영문과 숫자를 6자리 이상 설정 해 주세요", Toast.LENGTH_SHORT).show()
                binding.edtRegisterPassword.text.clear()
            // 문제가 없을 경우 통과
            } else if (password.isNotEmpty() && password.matches("^[A-Za-z0-9]{6,20}$".toRegex())) {
                passwordFlag = true
            }
        }

        // 비밀번호 확인란에 입력한 후에 다른 곳을 클릭하면
        binding.edtRegisterPasswordCheck.setOnFocusChangeListener { view, hasFocus ->
            passwordCheckFlag = false
            passwordCheck = binding.edtRegisterPasswordCheck.text.toString()
            // 만약 비밀번호 자체에서 문제가 있을 경우에는 비밀번호부터 올바르게 입력하도록 함
            if (!passwordFlag) {
                Toast.makeText(this, "먼저 비밀번호를 입력해 주세요", Toast.LENGTH_SHORT).show()
            // 패턴 검사
            } else if (!hasFocus && passwordCheck.isNotEmpty() && password != passwordCheck) {
                Toast.makeText(this, "비밀번호가 다릅니다 확인 후 다시 입력해 주세요", Toast.LENGTH_SHORT).show()
                binding.edtRegisterPasswordCheck.text.clear()
            // 문제가 없을 경우 통과
            } else if (passwordFlag && passwordCheck.isNotEmpty() && password == passwordCheck) {
                passwordCheckFlag = true
            }
        }

        // 이름란에 입력한 후에 다른 곳을 클릭하면
        binding.edtRegisterName.setOnFocusChangeListener { view, hasFocus ->
            nameFlag = false
            name = binding.edtRegisterName.text.toString()
            // 이름 패턴 검사
            if (!hasFocus && name.isNotEmpty() && !name.matches("^[가-힣]{2,5}$".toRegex())) {
                Toast.makeText(this, "이름은 한글이름으로 2~5자리 이내 입력해 주세요", Toast.LENGTH_SHORT).show()
                binding.edtRegisterName.text.clear()
            // 문제가 없을 경우 통과
            } else if (name.isNotEmpty() && name.matches("^[가-힣]{2,5}$".toRegex())) {
                nameFlag = true
            }
        }

        // 탄생년란에 입력한 후에 다른 곳을 클릭하면
        binding.edtRegisterBirthYear.setOnFocusChangeListener { view, hasFocus ->
            yearFlag = false
            year = binding.edtRegisterBirthYear.text.toString()
            // 탄생년 패턴검사(1900~올해)
            if (year != "") {
                val yearInt = year.toInt()
                var yearIntFlag = false
                val today = LocalDateTime.now()
                val thisYear = today.year
                if (yearInt in 1900..thisYear) {
                    yearIntFlag = true
                }
                if (!hasFocus && year.isNotEmpty() && !yearIntFlag) {
                    Toast.makeText(this, "정확한 탄생년을 입력해 주세요", Toast.LENGTH_SHORT).show()
                    binding.edtRegisterBirthYear.text.clear()
                // 문제가 없을 경우 통과
                } else if (year.isNotEmpty() && yearIntFlag) {
                    yearFlag = true
                }
            }
        }

        // 탄생월란에 입력한 후에 다른 곳을 클릭하면
        binding.edtRegisterBirthMonth.setOnFocusChangeListener { view, hasFocus ->
            monthFlag = false
            month = binding.edtRegisterBirthMonth.text.toString()
            // 탄생월 패턴 검사(1~12월 이내의 숫자를 입력)
            if (month != "") {
                val monthInt = month.toInt()
                var monthIntFlag = false
                if (monthInt in 1..12) {
                    monthIntFlag = true
                }
                if (!monthIntFlag && month.isNotEmpty()) {
                    Toast.makeText(this, "정확한 탄생월을 입력해 주세요", Toast.LENGTH_SHORT).show()
                    binding.edtRegisterBirthMonth.text.clear()
                // 문제가 없을 경우 통과
                } else if (monthIntFlag && month.isNotEmpty()) {
                    monthFlag = true
                }
            }
            if (month.length == 1) {
                month = "0$month"
            }
        }

        // 탄생일란에 입력한 후에 다른 곳을 클릭하면
        binding.edtRegisterBirthDay.setOnFocusChangeListener { view, hasFocus ->
            dayFlag = false
            day = binding.edtRegisterBirthDay.text.toString()
            // 탄생월에 따른 일자 범위 설정
            if (day != "") {
                val monthInt = month.toInt()
                val dayInt = day.toInt()
                val dayIntFlag = checkBirthDate(monthInt, dayInt)
                if (!dayIntFlag && !hasFocus && day.isNotEmpty()) {
                    Toast.makeText(this, "정확한 탄생일을 입력해 주세요", Toast.LENGTH_SHORT).show()
                    binding.edtRegisterBirthDay.text.clear()
                // 문제가 없을 경우 통과
                } else if (dayIntFlag && day.isNotEmpty()) {
                    dayFlag = true
                }
            }
            if (day.length == 1) {
                day = "0$day"
            }
        }

        // 라디오 버튼을 클릭할 경우 해당 gender값을 입력
        binding.rgGender.setOnCheckedChangeListener { radioGroup, select ->
            genderFlag = false
            when (select) {
                binding.rbMale.id -> {
                    gender = "남성"
                    genderFlag = true
                }
                binding.rbFemale.id -> {
                    gender = "여성"
                    genderFlag = true
                }
            }
        }

        // 전화번호란에 입력한 후에 다른 곳을 클릭하면
        binding.edtRegisterPhone.setOnFocusChangeListener { view, hasFocus ->
            phoneFlag = false
            phone = binding.edtRegisterPhone.text.toString()
            // 전화번호 패턴 검사
            if (!hasFocus && phone.isNotEmpty() && !phone.matches("^010[0-9]{4}[0-9]{4}$".toRegex())) {
                Toast.makeText(this, "- 없이 번호를 정확하게 입력해 주세요", Toast.LENGTH_SHORT).show()
                binding.edtRegisterPhone.text.clear()
            // 문제가 없을 경우 통과
            } else if (phone.isNotEmpty() && phone.matches("^010[0-9]{4}[0-9]{4}$".toRegex())) {
                phoneFlag = true
            }
        }

        binding.btnRegisterRegister.setOnClickListener {
            // 이메일은 제일 하단에 있어서 이메일 이후에 다른 란을 누를 가능성이 상대적으로 낮기 때문에 setOnFocusChangeListener를 사용하지 않음
            checkRegisterEmail()
            // 하나라도 문제가 있는데 그를 무시하고 Register 버튼을 누르게 되면 토스트 메시지 출력
            if (!idFlag) {
                Toast.makeText(this, "아이디를 정확하게 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (!passwordFlag) {
                Toast.makeText(this, "비밀번호를 정확하게 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (!passwordCheckFlag) {
                Toast.makeText(this, "비밀번호 확인을 정확하게 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (!nameFlag) {
                Toast.makeText(this, "이름을 정확하게 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (!yearFlag) {
                Toast.makeText(this, "탄생년을 정확하게 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (!monthFlag) {
                Toast.makeText(this, "탄생월을 정확하게 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (!dayFlag) {
                Toast.makeText(this, "탄생일을 정확하게 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (!genderFlag) {
                Toast.makeText(this, "성별을 정확하게 선택해주세요", Toast.LENGTH_SHORT).show()
            } else if (!phoneFlag) {
                Toast.makeText(this, "전화번호를 정확하게 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (!emailFlag) {
                checkRegisterEmail()
            } else {
                // 모두 통과하면 SQLite와 fireBase에 입력
                val shopDAO = ShopDAO()
                // 데이터를 Firebase에 넣는 척 하고 key 값을 받아옴
                val key = shopDAO.memberDatabaseReference?.push()?.key
                val birthDate = "${year}년 ${month}월 ${day}일"
                val member = Member(key, id, password, name, birthDate, gender, phone, email, bronze)
                val dbHelper = DBHelper(this, MainActivity.DB_NAME, MainActivity.VERSION)
                val insertFlag = dbHelper.insertMember(member)
                Log.d("kr.or.mrhi","RegisterActivity insertMember(SQLite) $insertFlag")
                if (insertFlag) {
                    shopDAO.memberDatabaseReference?.child(key!!)?.setValue(member)
                        ?.addOnSuccessListener {
                            Log.d("kr.or.mrhi","RegisterActivity insertMember(Firebase) OnSuccess")
                        }?.addOnFailureListener {
                            Log.d("kr.or.mrhi", "RegisterActivity insertMember(Firebase)OnFailure")
                        }
                }
                finish()
            }
        }

        // Cancel 버튼을 누르면 액티비티 종료
        binding.btnRegisterCancel.setOnClickListener {
            finish()
        }
    }

    // 월에 따른 날짜 범위 설정
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

    // checkBirthDate의 범위에 따른 날짜 true/false 리턴
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

    // 이메일 패턴 검사
    fun checkRegisterEmail() {
        emailFlag = false
        email = binding.edtRegisterEmail.text.toString()
        if (email.isNotEmpty() && !email.contains("@") && !(email.length > 10)){
            Toast.makeText(this,"이메일을 정확하게 입력해 주세요",Toast.LENGTH_SHORT).show()
            binding.edtRegisterEmail.text.clear()
        } else if (email.isNotEmpty() && email.contains("@") && email.length > 10) {
            emailFlag = true
        }
    }
}
