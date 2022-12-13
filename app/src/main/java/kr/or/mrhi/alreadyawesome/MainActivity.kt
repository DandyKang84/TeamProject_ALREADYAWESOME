package kr.or.mrhi.alreadyawesome

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import kr.or.mrhi.alreadyawesome.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        const val IN_BUSINESS = "01"
        const val DB_NAME = "alreadyAwesomeDB"
        var VERSION = 1
        const val TAB_HOME = "HOME"
        const val TAB_NEAR = "NEAR"
        const val TAB_MY = "MY"
    }
    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 액션바 대신 툴바 연결
        setSupportActionBar(binding.toolbar)
        // 자동적으로 생성되는 타이틀을 삭제
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 프래그먼트 연결
        addFragment()

        // intent를 통해서 받은 from 데이터를 통해서 어느 Fragment로부터 이동을 시작했는지를 파악 후, 그 Fragment로 이동
        if (intent.getStringExtra("from") != null) {
            val from = intent.getStringExtra("from").toString()
            changeFragment(from)
        }

        // EditActivity로부터 받은 데이터를 통해서 회원 정보가 정상적으로 수정되었는지를 판단 후, 토스트 메시지를 출력
        if (intent.getStringExtra("editUserData") == "SUCCESS") {
            Toast.makeText(this, "회원정보가 수정되었습니다", Toast.LENGTH_SHORT).show()
        }
    }


    // 오버플로우메뉴 설정
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            // 로그아웃 버튼을 누르면, LoginActivity로 이동
            R.id.menuLogout -> {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            // 회원정보수정 버튼을 누르면, EditActivity로 이동
            R.id.menuEdit -> {
              val intent = Intent(this, EditActivity::class.java)
              startActivity(intent)
              finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 직접 생성한 menu_overflow_home을 메뉴로 연결
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_overflow_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // 백버튼을 눌렀을 시의 이벤트 리스너
    override fun onBackPressed() {
        // AlertDialog에서 PositiveButton을 눌렀을 경우의 클릭 리스너
        val eventHandler = object: DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, answer: Int) {
                // PositiveButton이 눌리면 프로그램을 종료
                if(answer == DialogInterface.BUTTON_POSITIVE){
                    System.exit(0)
                }
            }
        }
        // AlertDialog를 생성
        AlertDialog.Builder(this).run {
            // AlertDialog의 메시지
            setMessage("프로그램을 종료하시겠습니까?")
            // Button 설정
            setPositiveButton("네", eventHandler)
            setNegativeButton("아니요", null)
            show()
        }
    }

    // TabLayout에 새로운 tab를 생성 및 Fragment와 연결
    fun addFragment() {
        // 디폴트가 되는 Fragment의 위치를 지정
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, HomeFragment())
            .commit()

        // tab을 생성(text, icon 지정) * 3
        val tab1: TabLayout.Tab = binding.tabLayout.newTab()
        createTab(tab1, TAB_HOME, R.drawable.ic_home)

        val tab2: TabLayout.Tab = binding.tabLayout.newTab()
        createTab(tab2, TAB_NEAR, R.drawable.ic_map)

        val tab3: TabLayout.Tab = binding.tabLayout.newTab()
        createTab(tab3, TAB_MY, R.drawable.ic_my)

        // Tab이 눌러졌을 때 Fragment를 변경하는 곳
        binding.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.text) {
                    TAB_HOME -> changeFragment(TAB_HOME)
                    TAB_NEAR -> changeFragment(TAB_NEAR)
                    TAB_MY -> changeFragment(TAB_MY)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // tab을 생성(text, icon 지정)
    fun createTab(tab: TabLayout.Tab, tabText: String, icon: Int) {
        tab.text = tabText
        tab.setIcon(icon)
        binding.tabLayout.addTab(tab)
    }

    // tab버튼이 눌리면 해당 Fragment로 이동하도록 하는 함수
    // 상기에서 입력한 text값을 기준으로 하여 tab을 구분 후 이동
    fun changeFragment(tab: String) {
        when (tab) {
            TAB_HOME -> {
                val tabIndex = binding.tabLayout.getTabAt(0)
                binding.tabLayout.selectTab(tabIndex)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, HomeFragment())
                    .commit()
            }
            TAB_NEAR -> {
                val tabIndex = binding.tabLayout.getTabAt(1)
                binding.tabLayout.selectTab(tabIndex)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, NearFragment())
                    .commit()
            }
            TAB_MY -> {
                val tabIndex = binding.tabLayout.getTabAt(2)
                binding.tabLayout.selectTab(tabIndex)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, MyFragment())
                    .commit()
            }
        }
    }
}