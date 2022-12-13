package kr.or.mrhi.alreadyawesome

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.sql.SQLException

class DBHelper(context: Context, dbName: String, version: Int) : SQLiteOpenHelper(context, dbName, null, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        val queryShop = """
            CREATE TABLE shop(
                shopId TEXT NOT NULL,
                shopName TEXT NOT NULL,
                type TEXT NOT NULL,
                address TEXT NOT NULL,
                shopPhone TEXT NOT NULL,
                latitude DOUBLE NOT NULL,
                longitude DOUBLE NOT NULL,
                openTime TEXT NOT NULL,
                closeTime TEXT NOT NULL,
                information TEXT NOT NULL,
                shopGrade DOUBLE NOT NULL,
                price1 INT NOT NULL,
                price2 INT NOT NULL,
                price3 INT NOT NULL,
                image TEXT NOT NULL
            );
        """.trimIndent()
        val queryMember = """
            CREATE TABLE member(
                memberKey TEXT NOT NULL,
                memberId TEXT NOT NULL,
                password TEXT NOT NULL,
                memberName TEXT NOT NULL,
                birthDate TEXT NOT NULL,
                gender TEXT NOT NULL,
                memberPhone TEXT NOT NULL,
                email TEXT NOT NULL,
                rate TEXT NOT NULL
            );
        """.trimIndent()
        val queryReview = """
            CREATE TABLE review(
                reviewKey TEXT NOT NULL,
                shopId TEXT NOT NULL,
                memberId TEXT NOT NULL,
                grade INT NOT NULL,
                date TEXT NOT NULL,
                content TEXT NOT NULL,
                menu TEXT NOT NULL
            );
        """.trimIndent()
        val queryReservation = """
            CREATE TABLE reservation(
                reserveKey NOT NULL,
                shopId TEXT NOT NULL,
                memberId TEXT NOT NULL,
                reserveDate INT NOT NULL,
                reserveTime TEXT NOT NULL,
                reserveMenu TEXT NOT NULL,
                price TEXT NOT NULL,
                payment TEXT NOT NULL
            );
        """.trimIndent()
        val queryLocation = """
            CREATE TABLE location(
                latitude DOUBLE NOT NULL,
                longitude DOUBLE NOT NULL
            )
        """.trimIndent()
        val queryUser = """
            CREATE TABLE user(
                userId TEXT NOT NULL
            )
        """.trimIndent()
        db?.execSQL(queryShop)
        db?.execSQL(queryMember)
        db?.execSQL(queryReview)
        db?.execSQL(queryReservation)
        db?.execSQL(queryLocation)
        db?.execSQL(queryUser)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val queryShop = """
            DROP TABLE shop
        """.trimIndent()
        val queryMember = """
            DROP TABLE member
        """.trimIndent()
        val queryReview = """
            DROP TABLE review
        """.trimIndent()
        val queryReservation = """
            DROP TABLE reservation
        """.trimIndent()
        val queryLocation = """
            DROP TABLE location
        """.trimIndent()
        val queryUser = """
            DROP TABLE user
        """.trimIndent()
        db?.execSQL(queryShop)
        db?.execSQL(queryMember)
        db?.execSQL(queryReview)
        db?.execSQL(queryReservation)
        db?.execSQL(queryLocation)
        db?.execSQL(queryUser)
        this.onCreate(db)
    }

    // shopTBL(INSERT/SELECT/UPDATE/DELETE) ======================================================================
    // 하기 select문을 실행하기 위해 데이터를 넣을 때 사용
    fun insertShop(shop: Shop) : Boolean {
        var flag = false
        val query = """
            INSERT INTO shop (shopId, shopName, type, address, shopPhone, latitude, longitude,
            openTime, closeTime, information, shopGrade, price1, price2, price3, image) 
            VALUES ('${shop.shopId}', '${shop.shopName}', '${shop.type}', '${shop.address}', 
            '${shop.shopPhone}', ${shop.latitude}, ${shop.longitude}, '${shop.openTime}', 
            '${shop.closeTime}', '${shop.information}', ${shop.shopGrade}, '${shop.price1}', 
            '${shop.price2}','${shop.price3}', '${shop.image}')
        """.trimIndent()
        val db = this.writableDatabase
        try {
            db.execSQL(query)
            flag = true
            Log.d("kr.or.mrhi", "insertShop() Success")
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "insertShop() ${e.printStackTrace()}")
            flag= false
        } finally {
            db.close()
        }
        return flag
    }

    // 검색 기능에 사용(검색창에 아무것도 입력하지 않았을 때)
    fun selectShopAll() : MutableList<Shop>? {
        var shopList: MutableList<Shop>? = mutableListOf<Shop>()
        var cursor: Cursor? = null
        val db = this.readableDatabase
        val query = """
            SELECT * FROM shop
        """.trimIndent()
        try {
            cursor = db.rawQuery(query, null)
            if (cursor.count > 0) {
                while(cursor.moveToNext()){
                    val shopId = cursor.getString(0)
                    val shopName = cursor.getString(1)
                    val type = cursor.getString(2)
                    val address = cursor.getString(3)
                    val shopPhone = cursor.getString(4)
                    val latitude = cursor.getDouble(5)
                    val longitude = cursor.getDouble(6)
                    val openTime = cursor.getString(7)
                    val closeTime = cursor.getString(8)
                    val information = cursor.getString(9)
                    val shopGrade = cursor.getFloat(10)
                    val price1 = cursor.getString(11)
                    val price2 = cursor.getString(12)
                    val price3 = cursor.getString(13)
                    val image = cursor.getString(14)
                    val shop = Shop(shopId, shopName, type, address, shopPhone, latitude, longitude,
                        openTime, closeTime, information, shopGrade, price1, price2, price3,image)
                    shopList?.add(shop)
                    Log.d("kr.or.mrhi", "selectShopAll() Success")
                }
            } else {
                shopList = null
                Log.d("kr.or.mrhi", "selectShopAll() shopList = null")
            }
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "selectShopAll() ${e.printStackTrace()}")
        } finally {
            cursor?.close()
            db.close()
        }
        return shopList
    }

    // nearFragment에서 marker를 클릭했을 때 dialog창에 해당 위치에 존재하는 매장의 정보를 뿌려줄 때 사용
    fun selectShopByShopLocation(selectShopName: String?, selectLatitude: Double, selectLongitude: Double) : MutableList<Shop>? {
        var shopList: MutableList<Shop>? = mutableListOf<Shop>()
        var cursor: Cursor? = null
        val db = this.readableDatabase
        val query = """
            SELECT * FROM shop WHERE shopName = '$selectShopName' AND 
            latitude = '$selectLatitude' AND longitude = '$selectLongitude'
        """.trimIndent()
        try {
            cursor = db.rawQuery(query, null)
            if (cursor.count > 0) {
                while(cursor.moveToNext()){
                    val shopId = cursor.getString(0)
                    val shopName = cursor.getString(1)
                    val type = cursor.getString(2)
                    val address = cursor.getString(3)
                    val shopPhone = cursor.getString(4)
                    val latitude = cursor.getDouble(5)
                    val longitude = cursor.getDouble(6)
                    val openTime = cursor.getString(7)
                    val closeTime = cursor.getString(8)
                    val information = cursor.getString(9)
                    val shopGrade = cursor.getFloat(10)
                    val price1 = cursor.getString(11)
                    val price2 = cursor.getString(12)
                    val price3 = cursor.getString(13)
                    val image = cursor.getString(14)
                    val shop = Shop(shopId, shopName, type, address, shopPhone, latitude, longitude,
                        openTime, closeTime, information, shopGrade, price1, price2, price3, image)
                    shopList?.add(shop)
                    Log.d("kr.or.mrhi", "searchShopByShopLocation() Success")
                }
            } else {
                shopList = null
                Log.d("kr.or.mrhi", "searchShopByShopLocation() shopList = null")
            }
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "searchShopByShopLocation() ${e.printStackTrace()}")
        } finally {
            cursor?.close()
            db.close()
        }
        return shopList
    }

    // review의 shopId나 reservation의 shopId로 shop데이터를 부를 때 사용
    fun selectShopById(selectShopId: String?) : MutableList<Shop>? {
        var shopList: MutableList<Shop>? = mutableListOf<Shop>()
        var cursor: Cursor? = null
        val db = this.readableDatabase
        val query = """
            SELECT * FROM shop WHERE shopId = '$selectShopId'
        """.trimIndent()
        try {
            cursor = db.rawQuery(query, null)
            if (cursor.count > 0) {
                while(cursor.moveToNext()){
                    val shopId = cursor.getString(0)
                    val shopName = cursor.getString(1)
                    val type = cursor.getString(2)
                    val address = cursor.getString(3)
                    val shopPhone = cursor.getString(4)
                    val latitude = cursor.getDouble(5)
                    val longitude = cursor.getDouble(6)
                    val openTime = cursor.getString(7)
                    val closeTime = cursor.getString(8)
                    val information = cursor.getString(9)
                    val shopGrade = cursor.getFloat(10)
                    val price1 = cursor.getString(11)
                    val price2 = cursor.getString(12)
                    val price3 = cursor.getString(13)
                    val image = cursor.getString(14)
                    val shop = Shop(shopId, shopName, type, address, shopPhone, latitude, longitude,
                        openTime, closeTime, information, shopGrade, price1, price2, price3, image)
                    shopList?.add(shop)
                    Log.d("kr.or.mrhi", "searchShopByShopId() Success")
                }
            } else {
                shopList = null
                Log.d("kr.or.mrhi", "searchShopByShopId() shopList = null")
            }
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "searchShopByShopId() ${e.printStackTrace()}")
        } finally {
            cursor?.close()
            db.close()
        }
        return shopList
    }

    // 검색 기능에 사용(검색창에 검색어를 입력했을 때)
    fun selectShopByQuery(searchQuery: String) : MutableList<Shop>? {
        var shopList: MutableList<Shop>? = mutableListOf<Shop>()
        var cursor: Cursor? = null
        val db = this.readableDatabase
        val query = """
            SELECT * FROM shop WHERE (shopName LIKE '%${searchQuery}%') OR (address Like '%${searchQuery}%')
        """.trimIndent()
        try {
            cursor = db.rawQuery(query, null)
            if (cursor.count > 0) {
                while(cursor.moveToNext()){
                    val shopId = cursor.getString(0)
                    val shopName = cursor.getString(1)
                    val type = cursor.getString(2)
                    val address = cursor.getString(3)
                    val shopPhone = cursor.getString(4)
                    val latitude = cursor.getDouble(5)
                    val longitude = cursor.getDouble(6)
                    val openTime = cursor.getString(7)
                    val closeTime = cursor.getString(8)
                    val information = cursor.getString(9)
                    val shopGrade = cursor.getFloat(10)
                    val price1 = cursor.getString(11)
                    val price2 = cursor.getString(12)
                    val price3 = cursor.getString(13)
                    val shop = Shop(shopId, shopName, type, address, shopPhone, latitude, longitude,
                        openTime, closeTime, information, shopGrade, price1, price2, price3)
                    shopList?.add(shop)
                    Log.d("kr.or.mrhi", "searchShopByQuery() Success}")
                }
            } else {
                shopList = null
                Log.d("kr.or.mrhi", "searchShopByQuery() shopList = null")
            }
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "searchShopByQuery() ${e.printStackTrace()}")
        } finally {
            cursor?.close()
            db.close()
        }
        return shopList
    }

    // 중복된 데이터가 들어가는 걸 방지하기 위해서 삭제할 때 사용
    fun deleteShopAll() : Boolean {
        var flag = false
        val query = """
            DELETE FROM shop
        """.trimIndent()
        val db = this.writableDatabase
        try {
            db.execSQL(query)
            flag = true
            Log.d("kr.or.mrhi", "deleteShopAll() Success")
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "deleteShopAll() ${e.printStackTrace()}")
            flag= false
        } finally {
            db.close()
        }
        return flag
    }

    // memberTBL(INSERT/SELECT/UPDATE/DELETE) ======================================================================
    // LoadActivity에서 Firebase로부터 데이터를 가져올 때와 RegisterActivity에서 신규가입하는 회원의 정보를 추가할 때 사용
    fun insertMember(member: Member) : Boolean {
        var flag = false
        val db = this.writableDatabase
        val query = """
            INSERT INTO member (memberKey, memberId, password, memberName, birthDate, 
            gender, memberPhone, email, rate) 
            VALUES ('${member.memberKey}', '${member.memberId}', '${member.password}', '${member.memberName}', 
            '${member.birthDate}', '${member.gender}', '${member.memberPhone}', '${member.email}', '${member.rate}')
        """.trimIndent()
        try {
            db.execSQL(query)
            flag = true
            Log.d("kr.or.mrhi", "insertMember() Success")
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "insertMember() ${e.printStackTrace()}")
            flag= false
        } finally {
            db.close()
        }
        return flag
    }

    // 로그인할 때 등록된 아이디 및 비밀번호가 맞는지 확인할 때 사용
    fun selectMemberForLogin(id: String, password: String) : Boolean {
        var flag = false
        var cursor: Cursor? = null
        val db = this.readableDatabase
        val query = """
            SELECT memberId, password FROM member WHERE memberId = '${id}' AND password = '${password}'
        """.trimIndent()
        try{
            cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()){
                if(cursor.getString(0).equals(id) && cursor.getString(1).equals(password)) flag = true
            }
            Log.d("kr.or.mrhi","selectLogin() Success")
        }catch (e: SQLException){
            Log.d("kr.or.mrhi","selectLogin() ${e.printStackTrace()}")
            flag = false
        }finally {
            cursor?.close()
            db.close()
        }
        return flag
    }

    // 회원가입시 중복된 아이디가 있는지 확인할 때 사용
    fun selectMemberCheckId(id: String) : Boolean {
        var flag = false
        var cursor: Cursor? = null
        val db = this.readableDatabase
        val query = """
            SELECT memberId FROM member WHERE memberId = '${id}'
        """.trimIndent()
        try {
            cursor = db.rawQuery(query,null)
            if (cursor.moveToFirst()){
                if(cursor.getString(0).equals(id)) flag = true
            }
            Log.d("kr.or.mrhi","selectMemberCheckId() Success")
        } catch (e: SQLException){
            Log.d("kr.or.mrhi","selectMemberCheckId() ${e.printStackTrace()}")
            flag = false
        } finally {
            cursor?.close()
            db.close()
        }
        return flag
    }

    // memberId 또는 userId -> memberList -> member를 구하고자 할 때 사용
    fun selectMemberById(selectMemberId: String?) : MutableList<Member>? {
        var memberList: MutableList<Member>? = mutableListOf<Member>()
        var cursor: Cursor? = null
        val db = this.readableDatabase
        val query = """
            SELECT * FROM member WHERE memberId = '$selectMemberId'
        """.trimIndent()
        try {
            cursor = db.rawQuery(query, null)
            if (cursor.count > 0) {
                while(cursor.moveToNext()){
                    val memberKey = cursor.getString(0)
                    val memberId = cursor.getString(1)
                    val password = cursor.getString(2)
                    val memberName = cursor.getString(3)
                    val birthDate = cursor.getString(4)
                    val gender = cursor.getString(5)
                    val memberPhone = cursor.getString(6)
                    val email = cursor.getString(7)
                    val rate = cursor.getString(8)
                    val member = Member(memberKey, memberId, password, memberName, birthDate,
                        gender, memberPhone, email, rate)
                    memberList?.add(member)
                    Log.d("kr.or.mrhi", "selectMemberById() Success")
                }
            } else {
                memberList = null
                Log.d("kr.or.mrhi", "selectMemberById() memberList = null")
            }
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "selectMemberById() ${e.printStackTrace()}")
        } finally {
            cursor?.close()
            db.close()
        }
        return memberList
    }

    // 중복된 데이터가 들어가는 걸 방지하기 위해서 삭제할 때 사용
    fun deleteMemberAll() : Boolean {
        var flag = false
        val query = """
            DELETE FROM member
        """.trimIndent()
        val db = this.writableDatabase
        try {
            db.execSQL(query)
            flag = true
            Log.d("kr.or.mrhi", "deleteMemberAll() Success")
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "deleteMemberAll() ${e.printStackTrace()}")
            flag= false
        } finally {
            db.close()
        }
        return flag
    }

    // locationTBL(INSERT/SELECT/UPDATE/DELETE) ======================================================================
    // LoginActivity에서 현재 위치를 입력할 때 사용
    fun insertMyLocation(latitude: Double, longitude: Double) : Boolean {
        var flag = false
        val query = """
            INSERT INTO location (latitude, longitude) VALUES ('$latitude', '$longitude')
        """.trimIndent()
        val db = this.writableDatabase
        try {
            db.execSQL(query)
            flag = true
            Log.d("kr.or.mrhi", "insertMyLocation() Success")
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "insertMyLocation() ${e.printStackTrace()}")
            flag= false
        } finally {
            db.close()
        }
        return flag
    }

    // NearFragment에서 입력되었던 위치를 불러올 때 사용
    fun selectMyLocation() : ArrayList<Double> {
        val locationList = ArrayList<Double>()
        var cursor: Cursor? = null
        val db = this.readableDatabase
        val query = """
            SELECT * FROM location
        """.trimIndent()
        try {
            cursor = db.rawQuery(query, null)
            if (cursor.count > 0) {
                while(cursor.moveToNext()){
                    val latitude = cursor.getDouble(0)
                    val longitude = cursor.getDouble(1)
                    locationList.add(0, latitude)
                    locationList.add(1, longitude)
                }
                Log.d("kr.or.mrhi", "selectLocation() Success")
            }
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "selectLocation() ${e.printStackTrace()}")
        } finally {
            cursor?.close()
            db.close()
        }
        return locationList
    }

    // userTBL(INSERT/SELECT/UPDATE/DELETE) ======================================================================
    // 로그인할 때 어떤 아이디로 로그인되었는지
    fun insertUser(userId: String) : Boolean {
        var flag = false
        val query = """
            INSERT INTO user (userId) VALUES ('$userId')
        """.trimIndent()
        val db = this.writableDatabase
        try {
            db.execSQL(query)
            flag = true
            Log.d("kr.or.mrhi", "insertUser() Success")
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "insertUser() ${e.printStackTrace()}")
            flag = false
        } finally {
            db.close()
        }
        return flag
    }

    // 로그인한 다음에 로그인한 아이디로 다른 데이터를 불러와야할 때 사용
    fun selectUser() : String {
        var memberId = ""
        var cursor: Cursor? = null
        val db = this.readableDatabase
        val query = """
            SELECT * FROM user
        """.trimIndent()
        try {
            cursor = db.rawQuery(query, null)
            if (cursor.count > 0) {
                while(cursor.moveToNext()){
                    memberId = cursor.getString(0)
                }
            }
            Log.d("kr.or.mrhi", "selectUser() Success")
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "selectUser() ${e.printStackTrace()}")
        } finally {
            cursor?.close()
            db.close()
        }
        return memberId
    }

    // LoginActivity 화면이 실행돼서 로그인 정보를 새로 받을 때 사용
    fun deleteUser() : Boolean {
        var flag = false
        val query = """
            DELETE FROM user
        """.trimIndent()
        val db = this.writableDatabase
        try {
            db.execSQL(query)
            flag = true
            Log.d("kr.or.mrhi", "deleteUser() Success")
        } catch (e: Exception) {
            Log.d("kr.or.mrhi", "deleteUser() ${e.printStackTrace()}")
            flag= false
        } finally {
            db.close()
        }
        return flag
    }
}
