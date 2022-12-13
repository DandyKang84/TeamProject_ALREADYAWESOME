package kr.or.mrhi.alreadyawesome

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
class Shop(
        var shopId: String? = "",
        var shopName: String = "",
        var type: String = "",
        var address: String = "",
        var shopPhone: String = "",
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var openTime: String = "",
        var closeTime: String = "",
        var information: String = "",
        val shopGrade: Float = 0F,
        val price1: String = "",
        val price2: String = "",
        val price3: String = "",
        val image: String = "") : Parcelable {

    companion object : Parceler<Shop> {
        override fun create(parcel: Parcel): Shop {
            return Shop(parcel)
        }

        override fun Shop.write(parcel: Parcel, flags: Int) {
            parcel.writeString(shopId)
            parcel.writeString(shopName)
            parcel.writeString(type)
            parcel.writeString(address)
            parcel.writeString(shopPhone)
            parcel.writeDouble(latitude)
            parcel.writeDouble(longitude)
            parcel.writeString(openTime)
            parcel.writeString(closeTime)
            parcel.writeString(information)
            parcel.writeFloat(shopGrade)
            parcel.writeString(price1)
            parcel.writeString(price2)
            parcel.writeString(price3)
            parcel.writeString(image)
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readFloat(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )
}


