package kr.or.mrhi.alreadyawesome

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kr.or.mrhi.alreadyawesome.databinding.DialogMapShopBinding
import kr.or.mrhi.alreadyawesome.databinding.FragmentNearBinding

class NearFragment : Fragment(),
    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {
    lateinit var binding: FragmentNearBinding
    lateinit var mainContext: Context
    lateinit var bounds: LatLngBounds
    lateinit var bitmapDrawable: BitmapDrawable
    var myLocation: Location = Location("My")
    var shopList: MutableList<Shop>? = mutableListOf<Shop>()
    var googleMap: GoogleMap? = null
    val latLngBounds = LatLngBounds.builder()
    var count = 0

    // onAttach를 통해서 MainActivity의 context를 불러옴
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNearBinding.inflate(inflater, container, false)
        // 구글맵을 어디에 출력할건지 위치를 출력
        val supportMapFragment = childFragmentManager.findFragmentById(binding.nearMapFragment.id) as SupportMapFragment
        // 구글맵 callback 신호를 어디서 받을건지 지정(여기로 받음)
        supportMapFragment.getMapAsync(this)
        return binding.root
    }

    // 맵이 준비되면 작동
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        // Marker를 클릭했을 때의 이벤트 리스너를 여기서 받겠다.
        this.googleMap!!.setOnMarkerClickListener(this)
        val dbHelper = DBHelper(mainContext, MainActivity.DB_NAME, MainActivity.VERSION)
        // LoginActivity에서 구글맵에 연결해서 가져온 내 위치 정보를 SQLite를 통해서 받음
        val locationList = dbHelper.selectMyLocation()
        myLocation.latitude = locationList[0]
        myLocation.longitude = locationList[1]
        // 지도에 내 위치를 찍기 위한 함수
        markMyLocation(myLocation.latitude, myLocation.longitude)
        // 내 위치를 기준으로 주변 매장과의 거리를 계산하고 이를 출력하기 위한 함수
        calculateDistance()
        // count = 내 위치를 기준으로 3km 안에 있는 매장의 수
        // 만약 3km 주변에 해당되는 매장이 0일 경우에는 모든 매장 내역과 토스트 메시지를 출력
        if (count == 0) {
            addAllMark()
            bounds = latLngBounds.build()
            Toast.makeText(mainContext, "현재 위치로부터 3km 범위내에 미용시설을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        // 만약 있을 경우에는 해당되는 매장만을 출력
        } else {
            bounds = latLngBounds.build()
        }
        // 해당되는 데이터가 안전하게 화면 내에 들어갈 수 있도록 여유로 40를 줌
        val padding = 40
        // 카메라 범위 내에 들어가는 매장 위치와, 여유 분을 제공
        val camera = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        // 카메라를 움직임
        this.googleMap!!.moveCamera(camera)
    }

    // LoginActivity에서 구한 내 위도와 경도에 해당되는 위치에 마커를 찍음
    fun markMyLocation(latitude: Double, longitude: Double) {
        // markerOptions에게 위도, 경도를 한 번에 전달하기 위해서 합침
        val myLatLng = LatLng(latitude, longitude)
        val markerOptions = MarkerOptions()
        // marker의 아이콘 지정
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        // marker가 찍힐 위치를 지정
        markerOptions.position(myLatLng)
        // marker의 이름을 지정
        markerOptions.title("현위치")
        // 지정한 내용에 따라서 marker 추가
        googleMap!!.addMarker(markerOptions)
    }

    // 내 위치를 기준으로 지정된 거리(3km) 내에 있는 매장을 출력
    fun calculateDistance(){
        val dbHelper = DBHelper(mainContext, MainActivity.DB_NAME, MainActivity.VERSION)
        // SQLite에 저장돼있는 shop 데이터를 모두 가져옴(for문 사용)
        shopList = dbHelper.selectShopAll()
        for (i in 0 until shopList!!.size) {
            // Location에 이름 부여
            val newLocation: Location = Location(shopList!![i].shopName)
            // shopName Location에 위도, 경도를 줘서 위치 지정
            newLocation.latitude = shopList!![i].latitude
            newLocation.longitude = shopList!![i].longitude
            // 내 위치와 shopName Location의 거리를 측정
            val distance : Float = myLocation.distanceTo(newLocation)
            // 거리가 3km 보다 작을 경우에 한해서 marker를 추가
            if (distance <= 3_000) {
                addMark(newLocation, shopList!![i].shopName, shopList!![i].type)
            }
        }
    }

    // distance가 3km보다 작다고 판단된 매장 위치에 한하여 marker를 찍기 위한 함수
    fun addMark(newLocation: Location, name: String, type: String) {
        // shopType에 따라서 아이콘을 다르게 찍음
        when(type) {
            HairActivity.HAIR_KO -> bitmapDrawable = getDrawable(mainContext, R.drawable.ic_mark_hair) as BitmapDrawable
            NailActivity.NAIL_KO -> bitmapDrawable = getDrawable(mainContext, R.drawable.ic_mark_nail) as BitmapDrawable
            MakeUpActivity.MAKEUP_KO -> bitmapDrawable = getDrawable(mainContext, R.drawable.ic_mark_makeup) as BitmapDrawable
            SkinCareActivity.SKINCARE_KO -> bitmapDrawable = getDrawable(mainContext, R.drawable.ic_mark_skincare) as BitmapDrawable
        }
        // 아이콘 사이즈 지정
        val scaleBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 50, 50, false)
        val descriptor = BitmapDescriptorFactory.fromBitmap(scaleBitmap)
        // markerOptions에게 위도, 경도를 한 번에 전달하기 위해서 합침
        val newLatLng = LatLng(newLocation.latitude, newLocation.longitude)
        val markerOptions = MarkerOptions()
        // marker의 기본 아이콘을 생성 후, 상기에서 선언한 이미지 및 사이즈로 지정
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        markerOptions.icon(descriptor)
        // marker를 찍을 위치를 지정
        markerOptions.position(newLatLng)
        // marker의 이름을지정
        markerOptions.title(name)
        // marker를 추가
        googleMap!!.addMarker(markerOptions)
        // 카메라 범위를 지정하기 위한 범위 내에 해당 위치를 추가
        latLngBounds.include(newLatLng)
        count += 1
    }

    // addMark와 동일한 형태이나, 3km 내의 매장 뿐만 아니라 모든 매장의 위치를 marker로 표시
    fun addAllMark() {
        val dbHelper = DBHelper(mainContext, MainActivity.DB_NAME, MainActivity.VERSION)
        shopList = dbHelper.selectShopAll()
        for (i in 0 until shopList!!.size) {
            val newLocation = Location(shopList!![i].shopName)
            newLocation.latitude = shopList!![i].latitude
            newLocation.longitude = shopList!![i].longitude
            when(shopList!![i].type) {
                HairActivity.HAIR_KO -> bitmapDrawable = getDrawable(mainContext, R.drawable.ic_mark_hair) as BitmapDrawable
                NailActivity.NAIL_KO -> bitmapDrawable = getDrawable(mainContext, R.drawable.ic_mark_nail) as BitmapDrawable
                MakeUpActivity.MAKEUP_KO -> bitmapDrawable = getDrawable(mainContext, R.drawable.ic_mark_makeup) as BitmapDrawable
                SkinCareActivity.SKINCARE_KO -> bitmapDrawable = getDrawable(mainContext, R.drawable.ic_mark_skincare) as BitmapDrawable
            }
            val scaleBitmap = Bitmap.createScaledBitmap(bitmapDrawable.bitmap, 50, 50, false)
            val descriptor = BitmapDescriptorFactory.fromBitmap(scaleBitmap)
            val newLatLng = LatLng(newLocation.latitude, newLocation.longitude)
            val markerOptions = MarkerOptions()
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            markerOptions.icon(descriptor)
            markerOptions.position(newLatLng)
            markerOptions.title(shopList!![i].shopName)
            googleMap!!.addMarker(markerOptions)
            latLngBounds.include(newLatLng)
        }
    }

    // marker를 클릭할 경우, 해당되는 매장의 정보가 담긴 다이얼로그 창을 띄움
    override fun onMarkerClick(marker: Marker): Boolean {
        val dbHelper = DBHelper(mainContext, MainActivity.DB_NAME, MainActivity.VERSION)
        // SQLite에서 해당 매장의 이름, 경도, 위도를 기준으로 검색을 해서 해당되는 데이터를 불러옴
        val shopList = dbHelper.selectShopByShopLocation(marker.title, marker.position.latitude, marker.position.longitude)
        val selectShop = shopList?.get(0)
        val dialogBinding = DialogMapShopBinding.inflate(LayoutInflater.from(mainContext))
        val builder = AlertDialog.Builder(mainContext)
        // shopImage를 불러오기 위해서 homeAdapter에서 만들었던 insertImage 함수를 사용
        val homeAdapter = HomeAdapter(mainContext, shopList)
        homeAdapter.insertImage(dialogBinding.ivDialogShopImage, selectShop)
        // dialog 창에 SQLite로부터 가져온 데이터를 뿌려줌
        dialogBinding.tvItemShopName.text = selectShop?.shopName
        dialogBinding.ratingBarShop.rating = selectShop?.shopGrade!!
        dialogBinding.tvItemOpen.text = selectShop.openTime
        dialogBinding.tvItemClose.text = selectShop.closeTime
        // 매장 타입에 따라서 간략한 메뉴 설명 내용을 달리함
        when(selectShop.type){
            HairActivity.HAIR_KO -> dialogBinding.tvItemShopMenu.text = "커트 : ${selectShop.price1}원 ~"
            NailActivity.NAIL_KO -> dialogBinding.tvItemShopMenu.text = "네일 : ${selectShop.price1}원 ~"
            MakeUpActivity.MAKEUP_KO -> dialogBinding.tvItemShopMenu.text = "디자이너 : ${selectShop.price1}원 ~"
            SkinCareActivity.SKINCARE_KO -> dialogBinding.tvItemShopMenu.text = "코스1 : ${selectShop.price1}원 ~"
        }
        dialogBinding.tvItemAddress.text = selectShop.address

        // 다이얼로그 창을 셋팅
        builder.setView(dialogBinding.root)
        val dialog: AlertDialog = builder.create()
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.show()

        // 매장으로 이동 버튼을 클릭할 시, ShopActivity로 이동
        // 해당 내용은 HomeAdapter에서 이동하는 경우와 동일한 형태
        dialogBinding.btnGoToShop.setOnClickListener {
            val intent = Intent(mainContext, ShopActivity::class.java)
            intent.putExtra("shop", selectShop)
            intent.putExtra("from", MainActivity.TAB_NEAR)
            binding.root.context.startActivity(intent)
            (mainContext as MainActivity).finish()
        }
        return true
    }
}