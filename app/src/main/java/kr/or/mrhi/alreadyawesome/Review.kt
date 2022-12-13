package kr.or.mrhi.alreadyawesome

data class Review(
    var reviewKey: String? = "",
    var shopId: String? = "",
    var memberId: String = "",
    var grade: Int = 0,
    var date: String = "",
    var content: String = "",
    var menu: String = ""
)