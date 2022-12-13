package kr.or.mrhi.alreadyawesome

data class Reservation(
    var reserveKey: String? = "",
    var shopId: String? = "",
    var memberId: String = "",
    var reserveDate: String = "",
    var reserveTime: String = "",
    var reserveMenu: String = "",
    var price: String = "",
    var payment: String = ""
)
