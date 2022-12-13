package kr.or.mrhi.alreadyawesome

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.TextView
import kr.or.mrhi.alreadyawesome.databinding.ActivityQnaBinding
import kr.or.mrhi.alreadyawesome.databinding.DialogQnaBinding

class QnAActivity : AppCompatActivity() {
    lateinit var binding: ActivityQnaBinding
    var from = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQnaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        questionClickEvent(binding.tvQnaQuestion1)
        questionClickEvent(binding.tvQnaQuestion2)
        questionClickEvent(binding.tvQnaQuestion3)
        questionClickEvent(binding.tvQnaQuestion4)
        questionClickEvent(binding.tvQnaQuestion5)
        questionClickEvent(binding.tvQnaQuestion6)

        // 문의하기를 포함한 해당 레이아웃을 클릭할 경우 임의의 번호(1577-0000)로 전화를 연결하기 위해서 이동
        binding.rlContactUs.setOnClickListener {
            val myUri = Uri.parse("tel:${MyFragment.CONTACT_US}")
            val intent = Intent(Intent.ACTION_DIAL, myUri)
            startActivity(intent)
        }

        // Close 버튼을 클릭할 경우 Activity를 종료함
        binding.btnQnaClose.setOnClickListener {
            finish()
        }
    }

    // 버튼을 포함한 레이아웃을 클릭할 경우 QnA가 다이얼로그 창에 출력됨
    fun questionClickEvent(selectQuestion: TextView) {
        selectQuestion.setOnClickListener {
            // NearFragment의 onMarkerClick과 동일한 형태
            val dialogBinding = DialogQnaBinding.inflate(LayoutInflater.from(this))
            val builder = AlertDialog.Builder(this)
            builder.setView(dialogBinding.root)
            dialogBinding.tvDialogQnaQuestion.text = selectQuestion.text.toString()
            when(selectQuestion){
                binding.tvQnaQuestion1 -> {
                    dialogBinding.tvDialogQnaAnswer.text = resources.getString(R.string.qna1)
                }
                binding.tvQnaQuestion2 -> {
                    dialogBinding.tvDialogQnaAnswer.text = resources.getString(R.string.qna2)
                }
                binding.tvQnaQuestion3 -> {
                    dialogBinding.tvDialogQnaAnswer.text = resources.getString(R.string.qna3)
                }
                binding.tvQnaQuestion4 -> {
                    dialogBinding.tvDialogQnaAnswer.text = resources.getString(R.string.qna4)
                }
                binding.tvQnaQuestion5 -> {
                    dialogBinding.tvDialogQnaAnswer.text = resources.getString(R.string.qna5)
                }
                binding.tvQnaQuestion6 -> {
                    dialogBinding.tvDialogQnaAnswer.text = resources.getString(R.string.qna6)
                }
            }
            val dialog: AlertDialog = builder.create()
            dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            // Close 버튼을 클릭할 경우 다이얼로그 창을 닫음
            dialogBinding.btnQnaClose.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }


}