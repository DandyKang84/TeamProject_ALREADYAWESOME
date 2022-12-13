package kr.or.mrhi.alreadyawesome

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import kr.or.mrhi.alreadyawesome.data.BeautyFacilities
import kr.or.mrhi.alreadyawesome.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class LoginActivity : AppCompatActivity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    companion object {
        const val ADMIN = "admin"
    }
    lateinit var binding: ActivityLoginBinding
    lateinit var providerClient: FusedLocationProviderClient
    lateinit var apiClient: GoogleApiClient
    lateinit var myLocation: Location
    var shopList: MutableList<Shop>? = mutableListOf<Shop>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dbHelper = DBHelper(this, MainActivity.DB_NAME, MainActivity.VERSION)
        // 로그인할 때 로그인 정보를 넣기 위해서 미리 SQLite를 비움
        var deleteFlag = dbHelper.deleteUser()
        Log.d("kr.or.mrhi", "LoginActivity deleteUser(SQLite) $deleteFlag")

        // LoadActivity로부터 count 값을 받고
        val count = intent.getIntExtra("count", 0)
        // 그 count 값이 0이 아니라면 = Firebase에 데이터가 있다면
        if (count != 0) {
            // 아이콘을 안 보이게 만듦
            binding.ivSettingData.visibility = View.INVISIBLE
        }
        Log.d("kr.or.mrhi", "intent count $count")

        // count값이 0이라서 아이콘이 사라지지 않았고, 만약 누를 경우
        binding.ivSettingData.setOnLongClickListener {
            val id = binding.edtLoginId.text.toString()
            val password = binding.edtLoginPassword.text.toString()
            // 로그인 아이디, 비밀번호 란에 ADMIN을 입력해야하고 맞을 경우,
            if (id == ADMIN && password == ADMIN) {
                // TextView는 클리어
                binding.edtLoginId.text.clear()
                binding.edtLoginPassword.text.clear()
                // 혹시 모를 오류를 방어하기 위해서 한 번 더 count가 0이라는 조건을 걺
                if (count == 0) {
                    // 데이터를 넣기 전에 먼저 SQLite의 shop을 초기화 시킴
                    deleteFlag = dbHelper.deleteShopAll()
                    if (deleteFlag) {
                        // SQLite가 초기화에 성공했으면 토스트 메시지를 출력함과 동시에 OPEN API로부터 데이터를 가져오는 작업이 시작됨
                        Toast.makeText(this, "OPEN API로부터 데이터를 가져옵니다.", Toast.LENGTH_SHORT).show()
                        Log.d("kr.or.mrhi", "LoginActivity getBeautyFacilities")
                        // Retrofit 객체를 생성
                        val retrofit = Retrofit.Builder()
                            // 통신할 서버 주소를 설정
                            .baseUrl(SeoulOpenAPI.DOMAIN)
                            // 통신 시에 주고받는 데이터 형태를 Gson으로 변환한다고 설정
                            .addConverterFactory(GsonConverterFactory.create())
                            // 위의 설정을 바탕으로 생성
                            .build()
                        // SeoulOpenService에 만들어 놓은 인터페이스에 값을 입력
                        val service = retrofit.create(SeoulOpenService::class.java)
                        service.getBeautyFacilities(SeoulOpenAPI.API_KEY, SeoulOpenAPI.LIMIT)
                            // 클라이언트 객체가 제공하는
                            .enqueue(object : Callback<BeautyFacilities> {
                                // 연결에 성공했을 경우
                                override fun onResponse(
                                    call: Call<BeautyFacilities>,
                                    response: Response<BeautyFacilities>
                                ) {
                                    val data = response.body()
                                    data?.let {
                                        for (beautyFacilities in it.LOCALDATA_051801.row) {
                                            // 영업상태코드가 01(영업/정상)인 데이터에 한해서 가져옴
                                            if (beautyFacilities.TRDSTATEGBN == MainActivity.IN_BUSINESS) {
                                                val shopName = beautyFacilities.BPLCNM
                                                val type = beautyFacilities.UPTAENM
                                                // 업태 구분이 4가지 중 하나인 경우에 한하여 가져옴
                                                if (type == HairActivity.HAIR_KO || type == NailActivity.NAIL_KO || type == MakeUpActivity.MAKEUP_KO || type == SkinCareActivity.SKINCARE_KO) {
                                                    val address = beautyFacilities.RDNWHLADDR
                                                    var shopPhone = beautyFacilities.SITETEL
                                                    // 전화번호가 있고, 앞자리가 0이 아닌 경우에는 02를 붙여서 전화번호를 생성
                                                    if (shopPhone.isNotBlank()) {
                                                        if (!(shopPhone.substring(0 until 1) == "0")) {
                                                            shopPhone = "02$shopPhone"
                                                        }
                                                    // 전화번호가 등록되지 않은 경우에는 등록된 전화번호가 없다고 입력
                                                    } else {
                                                        shopPhone = "등록된 전화번호가 없습니다"
                                                    }
                                                    // 층수 등의 세부 주소는 좌표를 얻는 데 방해가 되기 때문에 아래 내용을 삭제함
                                                    val editAddress = EditAddress(beautyFacilities.RDNWHLADDR)
                                                    // 주소를 가지고 경도와 위도를 가지는 location 값을 얻음
                                                    val location = getLatLng(editAddress)
                                                    // 얻은 Location에서 경도, 위도를 추출해서 각각의 값에 입력
                                                    val latitude = location.latitude
                                                    val longitude = location.longitude
                                                    // 아래 8가지 값은 API에는 없고 자체적으로 생성한 값
                                                    val openTime = "10:00"
                                                    val closeTime = "20:00"
                                                    val information = "매장소개는 현재 준비중입니다"
                                                    val shopGrade = 0F
                                                    // 업태구분에 따라서 일정 범위를 주고 랜덤값으로 가격을 생성
                                                    val price1 = makeRandomPrice(type, 1)
                                                    val price2 = makeRandomPrice(type, 2)
                                                    val price3 = makeRandomPrice(type, 3)
                                                    var image = ""
                                                    // 업태구분에 따라서 불러올 이미지명을 랜덤으로 생성
                                                    when (type) {
                                                        HairActivity.HAIR_KO -> {
                                                            val number = makeRandomNumberForImage(1, 12)
                                                            image = "img_hair_salon_$number"
                                                        }
                                                        NailActivity.NAIL_KO -> {
                                                            val number = makeRandomNumberForImage(1, 7)
                                                            image = "img_nail_shop_$number"
                                                        }
                                                        MakeUpActivity.MAKEUP_KO -> {
                                                            val number = makeRandomNumberForImage(1, 12)
                                                            image = "img_makeup_shop_$number"
                                                        }
                                                        SkinCareActivity.SKINCARE_KO -> {
                                                            val number = makeRandomNumberForImage(1, 3)
                                                            image = "img_skincare_shop_$number"
                                                        }
                                                    }
                                                    val shopDAOAPI = ShopDAO()
                                                    // 데이터를 넣는 척 하고 key값을 받아옴
                                                    val shopId = shopDAOAPI.shopDatabaseReference?.push()?.key
                                                    val shop = Shop(
                                                        shopId,
                                                        shopName,
                                                        type,
                                                        address,
                                                        shopPhone,
                                                        latitude,
                                                        longitude,
                                                        openTime,
                                                        closeTime,
                                                        information,
                                                        shopGrade,
                                                        price1,
                                                        price2,
                                                        price3,
                                                        image
                                                    )
                                                    // 상기에서 얻은 key(shopId)값에 데이터를 Firebase에 입력함
                                                    shopDAOAPI.shopDatabaseReference?.child(shopId!!)?.setValue(shop)
                                                        ?.addOnSuccessListener {
                                                            Log.d("kr.or.mrhi","LoginActivity insertShop()(Firebase) OnSuccess"
                                                            )
                                                        }?.addOnFailureListener {
                                                            Log.d("kr.or.mrhi","LoginActivity insertShop()(Firebase) OnFailure"
                                                            )
                                                        }
                                                    // Firebase뿐만 아니라 SQLite에도 데이터를 입력함
                                                    dbHelper.insertShop(shop)
                                                    shopList = dbHelper.selectShopAll()
                                                    Log.d("kr.or.mrhi","LoginActivity insertShop()(SQLite)"
                                                    )
                                                }
                                            }
                                        }
                                    // 통신 연결에는 성공했으나, 가져올 데이터가 없을 경우
                                    } ?: let {
                                        Log.d("kr.or.mrhi", "LoginActivity Data is empty")
                                    }
                                }

                                // 통신 연결에 실패했을 경우
                                override fun onFailure(call: Call<BeautyFacilities>, t: Throwable) {
                                    Log.d("kr.or.mrhi","LoginActivity getBeautyFacilities onFailure ${t.printStackTrace()}"
                                    )
                                }
                            })
                    }
                } else {
                    Toast.makeText(this, "이미 데이터가 존재합니다.", Toast.LENGTH_SHORT).show()
                }
                // 데이터를 모두 가져왔으면 아이콘을 사라지게 함
                binding.ivSettingData.visibility = View.INVISIBLE
            }
            true
        }
        // 구글맵과 연결
        connectWithGoogleMap()

        // 로그인 버튼을 누르면
        binding.btnLoginLogin.setOnClickListener {
            val id = binding.edtLoginId.text.toString()
            val password = binding.edtLoginPassword.text.toString()

           // SQLite에 입력된 아이디와 비밀번호와 일치하는지 확인 후, 어플내에서 사용하기 위해서 id값을 SQLite에 저장
           // MainActivity로 이동
           if (dbHelper.selectMemberForLogin(id, password) && dbHelper.insertUser(id)){
               Toast.makeText(this,"${binding.edtLoginId.text.toString()}님 환영합니다.",Toast.LENGTH_SHORT).show()
               val intent = Intent(this, MainActivity::class.java)
               startActivity(intent)
               finish()
            } else {
                // 아이디나 비밀번호가 다를 경우에는 토스트 메시지 출력
                Toast.makeText(this,"아이디와 비밀번호를 입력해 주세요",Toast.LENGTH_SHORT).show()
            }
        }

        // Register 버튼을 누르면 RegisterActivity로 이동
        binding.btnLoginRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // 층수 등의 세부 주소는 좌표를 얻는 데 방해가 되기 때문에 아래 내용을 삭제하기 위한 함수
    fun EditAddress(address: String): String {
        // ','을 기준으로 2개의 값으로 나뉘는데 그 중 앞 부분만 리턴한다는 의미
        val editAddress = address.split(",")
        return editAddress[0]
    }

    // Geocoder 클래스를 사용해서 주소에서 위도, 경도를 생성함
    fun getLatLng(address: String): LatLng {
        val geoCoder = Geocoder(this@LoginActivity, Locale.KOREA)
        // geoCoder에 변환하고자 하는 주소를 입력 (maxResults는 정확도와 연관이 있다고 함)
        val list = geoCoder.getFromLocationName(address, 1)
        // 위도, 경도를 입력받을 location을 생성 후, 임의의 값을 입력
        var location = LatLng(37.554891, 126.970814)

        if(list != null){
            // geoCoder로부터 받아온 값이 하나도 없다면 로그값 출력
            if (list.size == 0){
                Log.d("kr.or.mrhi", "해당 주소로 찾는 위도 경도가 없습니다. 올바른 주소를 입력해주세요.")
            } else {
                // geoCoder로부터 값을 받아왔다면 값의 위도와 경도를 각각 입력하여 location을 리턴함
                val addressLatLng = list[0]
                location = LatLng(addressLatLng.latitude, addressLatLng.longitude)
                return location
            }
        }
        return location
    }

    // 업태구분에 따른 랜덤값 생성
    fun makeRandomPrice(shopType: String, priceType: Int): String {
        var price = ""
        when(shopType) {
            HairActivity.HAIR_KO -> {
                when(priceType){
                    1 -> price = makeRandomNumberForPrice(2, 4)
                    2 -> price = makeRandomNumberForPrice(10, 25)
                    3 -> price = makeRandomNumberForPrice(8, 20)
                }
            }
            NailActivity.NAIL_KO -> {
                when(priceType){
                    1 -> price = makeRandomNumberForPrice(5, 10)
                    2 -> price = makeRandomNumberForPrice(5, 10)
                    3 -> price = makeRandomNumberForPrice(3, 8)
                }
            }
            MakeUpActivity.MAKEUP_KO -> {
                when(priceType){
                    1 -> price = makeRandomNumberForPrice(15, 20)
                    2 -> price = makeRandomNumberForPrice(10, 15)
                    3 -> price = makeRandomNumberForPrice(5, 10)
                }
            }
            SkinCareActivity.SKINCARE_KO -> {
                when(priceType){
                    1 -> price = makeRandomNumberForPrice(5, 10)
                    2 -> price = makeRandomNumberForPrice(10, 15)
                    3 -> price = makeRandomNumberForPrice(15, 20)
                }
            }
        }
        return price
    }

    // makeRandomPrice에서 입력한 기준에 따라서 랜덤값을 생성함
    fun makeRandomNumberForPrice(min: Int, max: Int): String {
        val number1: String
        val number2: String
        val number = ((Math.random() * (max - min + 1) + min).toInt()*10_000).toString()
        // 천원 단위로 ','를 찍기 위함
        if (number.length == 5) {
            number1 = number.substring(0, 2)
            number2 = number.substring(2)
        } else {
            number1 = number.substring(0, 3)
            number2 = number.substring(3)
        }
        return "${number1},${number2}"
    }

    // 업태구분에 따라서 이미지 갯수에 차이가 있기 때문에 그에 맞춰서 랜덤값을 생성
    fun makeRandomNumberForImage(min: Int, max: Int): String {
        var number = (Math.random() * (max - min + 1) + min).toInt().toString()
        if(number.length < 2) {
            number = "0$number"
        }
        return number
    }

    // 구글맵과 연결 시도
    fun connectWithGoogleMap() {
        // 복수의 퍼미션을 동시에 요청
        val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
        ){
            // 모두 허가가 됐으면 apiClient 연결
            if(it.all { permission -> permission.value }) {
                apiClient.connect()
            } else {
                // 그렇지 않을 경우에는 토스트 메시지 출력 후 어플 종료
                Toast.makeText(this, "권한거부로 인하여 앱을 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        // 위치정보를 제공하는 클라이언트 객체를 여기서 실행
        providerClient = LocationServices.getFusedLocationProviderClient(this)
        // api클라이언트 객체를 생성
        apiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        // 만약 하단의 4가지 퍼미션이 허가되지 않았으면 허가를 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        } else {
            // 이미 퍼미션이 허가돼있으면 apiClient 연결
            apiClient.connect()
        }
    }

    // 구글맵과 연결이 됐으면
    override fun onConnected(p0: Bundle?) {
        // 작업에 앞서서 퍼미션이 허가된 게 맞는지 한 번 더 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 최신 위치에 대한 위치값을 받음
            providerClient.lastLocation.addOnSuccessListener(object: OnSuccessListener<Location> {
                override fun onSuccess(location: Location?) {
                    location?.let {
                        myLocation = location
                        // 얻은 최신 값을 MainActivity의 NearFragment에서 사용하기 위해서 SQLite에 보관
                        val dbHelper = DBHelper(this@LoginActivity, MainActivity.DB_NAME, MainActivity.VERSION)
                        val insertFlag = dbHelper.insertMyLocation(myLocation.latitude, myLocation.longitude)
                        Log.d("kr.or.mrhi", "insertMyLocation(SQLite) $insertFlag")
                    }
                }
            })
        }
        // 사용이 완료됐으면 apiClient 종료
        apiClient.disconnect()
    }

    override fun onConnectionSuspended(data: Int) {
        Log.d("kr.or.mrhi", "onConnectionSuspended ${data}")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d("kr.or.mrhi", "onConnectionFailed ${connectionResult.errorMessage}")
    }
}

