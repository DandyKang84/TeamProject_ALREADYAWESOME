package kr.or.mrhi.alreadyawesome

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kr.or.mrhi.alreadyawesome.*
import kr.or.mrhi.alreadyawesome.databinding.ActivityReviewAddBinding
import kr.or.mrhi.alreadyawesome.jeon.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class ReviewAddActivity : AppCompatActivity() {
    lateinit var binding: ActivityReviewAddBinding
    var requestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){}
    var shopList : MutableList<Parcelable>? = null
    var shop : Shop? = null
    var imageUri: Uri? = null
    var from: String? = ""
    var reviewKey: String? = ""
    var shopId = ""
    var memberId = ""
    var grade = 0
    var date = ""
    var content = ""
    var menu = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //이미지를 갤러리에 요청
        requestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                Glide.with(applicationContext).load(it.data?.data)
                    .centerCrop()
                    .into(binding.ivDialogReviewImage)
                imageUri = it.data?.data
                val cursor = contentResolver.query(
                    it.data?.data as Uri,
                    arrayOf<String>(MediaStore.Images.Media.DATA),
                    null,
                    null,
                    null
                )
                cursor?.moveToFirst().let {
                }
            }
        }

        if (intent.getParcelableExtra<Shop>("shop") != null) {
            shop = intent.getParcelableExtra<Shop>("shop")
            shopId = shop?.shopId.toString()
        }

        if (intent.getStringExtra("from") != null) {
            from = intent.getStringExtra("from")
        }

        val shopName = shop?.shopName
        binding.tvDialogReviewShopName.text = shopName

        // shopType에 따라서 선택할 수 있는 메뉴명이 달라짐
        binding.tvDialogReviewMenu.setOnClickListener {
            when (shop?.type) {
                HairActivity.HAIR_KO -> {
                    val menus = arrayOf<String>("커트", "파마", "염색")
                    menuChoice(menus)
                }
                NailActivity.NAIL_KO -> {
                    val menus = arrayOf<String>("네일", "패디", "케어")
                    menuChoice(menus)
                }
                MakeUpActivity.MAKEUP_KO -> {
                    val menus = arrayOf<String>("원장님", "수석실장님", "디자이너")
                    menuChoice(menus)
                }
                SkinCareActivity.SKINCARE_KO -> {
                    val menus = arrayOf<String>("코스1", "코스2", "코스3")
                    menuChoice(menus)
                }
            }
        }

        // 클릭할 경우, 갤러리로부터 이미지를 가져올 수 있음
        binding.ivDialogReviewImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            requestLauncher.launch(intent)
        }

        // Close 버튼을 클릭할 경우, ReviewActivity로 이동
        binding.btnReviewClose.setOnClickListener {
            backToReviewActivity()
        }

        // Save 버튼을 클릭할 경우, 데이터 저장에 앞서서 모든 내용이 빠짐 없이 입력되었는지를 확인
        binding.btnReviewSave.setOnClickListener {
            val dbHelper = DBHelper(this, MainActivity.DB_NAME, MainActivity.VERSION)
            memberId = dbHelper.selectUser()
            menu = binding.tvDialogReviewMenu.text.toString()
            grade = binding.ratingBarDialogReview.rating.toInt()
            // 오늘 날짜를 yyyy-MM-dd의 형태로 생성
            date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            content = binding.edtDialogReviewContent.text.toString()

            // 메뉴나 내용이 선택되지 않았을 경우에는 해당 내용을 입력하라고 토스트 메시지 출력
            if (menu == "메뉴를 선택하세요") {
                Toast.makeText(this, "시술받으신 메뉴를 선택하세요", Toast.LENGTH_SHORT).show()
            } else if (content.isBlank()){
                Toast.makeText(this, "내용을 입력하세요", Toast.LENGTH_SHORT).show()
            } else if (grade == 0) {
                // 만약 ratingBar의 별도의 조작하지 않고 0점 그대로 두면 일부러 설정하지 않은 것인지를 확인 차 물어봄
                val eventHandler = object: DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, answer: Int) {
                        if(answer == DialogInterface.BUTTON_POSITIVE){
                            insertReview()
                        }
                    }
                }
                androidx.appcompat.app.AlertDialog.Builder(this).run {
                    setMessage("${shopName}의 평점이 0점이 맞으신가요?")
                    setPositiveButton("네", eventHandler)
                    setNegativeButton("아니요", null)
                    show()
                }
            } else {
                // 이상이 없으면 작성된 review를 입력
                insertReview()
            }
        }
    }

    fun insertReview() {
        val shopDAO = ShopDAO()
        // 데이터를 Firebase에 넣는 척을 하면서 key값을 받음
        reviewKey = shopDAO.reviewDatabaseReference?.push()?.key
        val review = Review(reviewKey, shopId, memberId, grade, date, content, menu)
        // 이미지가 있는 경우에는 이미지와 review를 모두 입력
        if (imageUri != null) {
            // 이미지와 데이터를 Firebase에 입력
            val imgReference = shopDAO.storage?.reference?.child("reviewImage/${reviewKey}.jpg")
            imgReference?.putFile(imageUri!!)?.addOnSuccessListener {
                Log.d("kr.or.mrhi", "${memberId}님의 리뷰 사진이 성공적으로 업로드되었습니다.")
                shopDAO.reviewDatabaseReference?.child(reviewKey!!)?.setValue(review)
                    ?.addOnSuccessListener {
                        Log.d("kr.or.mrhi", "${memberId}님의 리뷰가 성공적으로 등록되었습니다.")
                    }?.addOnFailureListener {
                        Log.d("kr.or.mrhi", "Review Insert Failure")
                    }
            }?.addOnFailureListener {
                Log.d("kr.or.mrhi", "Review Image Upload Failure")
            }
        // 이미지가 없는 경우에는 review만 입력
        } else {
            shopDAO.reviewDatabaseReference?.child(reviewKey!!)?.setValue(review)
                ?.addOnSuccessListener {
                    Log.d("kr.or.mrhi", "${memberId}님의 리뷰가 성공적으로 등록되었습니다.")
                }?.addOnFailureListener {
                    Log.d("kr.or.mrhi", "Review Insert Failure")
                }
        }
        // 모든 동작을 완료했으면 Activity를 종료하고 ReviewActivity로 돌아감
        backToReviewActivity()
    }

    // 메뉴를 선택하세요를 클릭할 경우 AlertDialog창이 출력
    // item을 클릭할 경우 해당 index를 인식해서 Array<String>중 몇 번째 값인지를 인식하고 TextView의 text를 수정함
    fun menuChoice(menus: Array<String>){
        var selectPosition = 0
        val eventHandler = object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, answer: Int) {
                if (answer == DialogInterface.BUTTON_POSITIVE) {
                    binding.tvDialogReviewMenu.text = menus.get(selectPosition)
                }
            }
        }
        AlertDialog.Builder(this).run {
            setTitle("메뉴선택")
            setIcon(R.drawable.ic_menu)
            setSingleChoiceItems(menus, 0, object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, index: Int) {
                    selectPosition = index
                }
            })
            setPositiveButton("닫기", eventHandler)
            setCancelable(false)
            show()
        }
            .setCanceledOnTouchOutside(true)
    }

    // 백버튼을 누르면 현 액티비티를 종료시키고 ReviewActivity로 돌아감
    override fun onBackPressed() {
        super.onBackPressed()
        backToReviewActivity()
    }

    fun backToReviewActivity() {
        val intent = Intent(this, ReviewActivity::class.java)
        intent.putExtra("shop", shop)
        intent.putExtra("from", from)
        startActivity(intent)
        finish()
    }
}