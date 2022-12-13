package kr.or.mrhi.alreadyawesome

import kr.or.mrhi.alreadyawesome.data.BeautyFacilities
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

class SeoulOpenAPI {
    companion object {
        const val DOMAIN = "http://openapi.seoul.go.kr:8088/"
        const val API_KEY = "586f557748636573383178525a7878"
        const val LIMIT = 50
    }
}

interface SeoulOpenService{
    @GET("{api_key}/json/LOCALDATA_051801/1/{end}/")
    fun getBeautyFacilities(@Path("api_key") key: String, @Path("end") limit: Int) : Call<BeautyFacilities>
}