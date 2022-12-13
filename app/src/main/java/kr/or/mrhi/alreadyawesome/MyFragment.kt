package kr.or.mrhi.alreadyawesome

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kr.or.mrhi.alreadyawesome.*
import kr.or.mrhi.alreadyawesome.databinding.DialogTermsOfUseBinding
import kr.or.mrhi.alreadyawesome.databinding.FragmentMyBinding

class MyFragment : Fragment() {
    companion object {
        const val CONTACT_US = "1577-0000"
    }
    lateinit var binding: FragmentMyBinding
    lateinit var mainContext: Context
    var memberList: MutableList<Member> = mutableListOf()

    // onAttach를 통해서 MainActivity의 context를 불러옴
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainContext = context
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyBinding.inflate(inflater, container, false)
        val shopDAO = ShopDAO()
        // 로그인 할 때 입력했던 userId 값을 기준으로 user(member)정보를 가져옴
        val dbHelper = DBHelper(mainContext, MainActivity.DB_NAME, MainActivity.VERSION)
        val id = dbHelper.selectUser()

        shopDAO.selectMember()?.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val member = data.getValue(Member::class.java)
                    if (member != null) {
                        if (member.memberId == id) {
                            memberList.add(member)
                        }
                        Log.d("kr.or.mrhi", "MainActivity selectMember(Firebase) onDataChange")
                    }
                }
                val user = memberList[0]
                val userName = user.memberName
                // 해당되는 userName(memberName)을 TextView에 출력
                binding.tvMyName.text = userName
                // 해당되는 rankImage를 해당되는 위치에 출력
                insertUserImage(user)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("kr.or.mrhi", "MainActivity selectMember(Firebase) onCancelled")
            }
        })

        // ReservationListActivity로 이동
        binding.tvMyReservation.setOnClickListener {
            val intent = Intent(mainContext, ReservationListActivity::class.java)
            startActivity(intent)
            (mainContext as MainActivity).finish()
        }

        // QnAActivity로 이동
        binding.tvMyQnA.setOnClickListener {
            val intent = Intent(mainContext, QnAActivity::class.java)
            startActivity(intent)
        }

        // 임의의 번호(1577-0000)로 전화를 연결하기 위해서 이동
        binding.tvMyContactUs.setOnClickListener {
            val myUri = Uri.parse("tel:$CONTACT_US")
            val intent = Intent(Intent.ACTION_DIAL, myUri)
            startActivity(intent)
        }

        // 다이얼로그 창을 출력
        binding.tvMyTermsAndUse.setOnClickListener {
            // 어느 화면을 출력할 건지 선택
            val dialogTermsBinding = DialogTermsOfUseBinding.inflate(LayoutInflater.from(mainContext))
            // 어디에 출력할 건지 Builder를 생성
            val builder = AlertDialog.Builder(mainContext)
            // 둘을 연결
            builder.setView(dialogTermsBinding.root)
            // 이를 생성함
            val dialogTerms: AlertDialog = builder.create()
            // 화면의 크기를 설정
            dialogTerms.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            // 백버튼을 허가할 건지 선택(불허가)
            dialogTerms.setCancelable(false)
            // 화면 외부를 눌렀을 때 화면이 꺼지게 할 건지(불허가)
            dialogTerms.setCanceledOnTouchOutside(false)
            // 다이얼로그 창 출력
            dialogTerms.show()

            // close버튼을 누르면 다이얼로그창을 닫음
            dialogTermsBinding.btnDialogTermsClose.setOnClickListener {
                dialogTerms.dismiss()
            }
        }
        return binding.root
    }

    // 해당되는 rankImage를 해당되는 위치에 출력하기 위한 함수
    fun insertUserImage(user: Member) {
        val shopDAO = ShopDAO()
        val imgRef = shopDAO.storage!!.reference.child("rateImage/img_rank_${user.rate}.jpg")
        imgRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Glide.with(mainContext)
                    .load(it.result)
                    .into(binding.ivMyProfileImage)
            }
        }
    }
}