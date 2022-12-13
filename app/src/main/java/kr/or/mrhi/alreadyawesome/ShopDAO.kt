package kr.or.mrhi.alreadyawesome

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class ShopDAO {
    var storage: FirebaseStorage? = null
    var shopDatabaseReference: DatabaseReference? = null
    var memberDatabaseReference: DatabaseReference? = null
    var reviewDatabaseReference: DatabaseReference? = null
    var reservationDatabaseReference: DatabaseReference? = null
    init{
        val db: FirebaseDatabase = FirebaseDatabase.getInstance()
        shopDatabaseReference = db.getReference("shop")
        memberDatabaseReference = db.getReference("member")
        reviewDatabaseReference = db.getReference("review")
        reservationDatabaseReference = db.getReference("reservation")
        storage = Firebase.storage
    }

    fun insertShop(shop: Shop?): Task<Void> {
        return shopDatabaseReference!!.push().setValue(shop)
    }

    fun insertMember(member: Member?): Task<Void> {
        return memberDatabaseReference!!.push().setValue(member)
    }

    fun insertReview(review: Review?): Task<Void> {
        return reviewDatabaseReference!!.push().setValue(review)
    }

    fun insertReservation(reservation: Reservation?): Task<Void> {
        return reservationDatabaseReference!!.push().setValue(reservation)
    }

    fun selectShop() : Query? {
        return shopDatabaseReference
    }

    fun selectMember() : Query? {
        return memberDatabaseReference
    }

    fun selectReview() : Query? {
        return reviewDatabaseReference
    }

    fun selectReservation() : Query? {
        return reservationDatabaseReference
    }

    fun updateShop(shopId: String, hashMap: HashMap<String, Any?>) : Task<Void> {
        return shopDatabaseReference!!.child(shopId).updateChildren(hashMap)
    }

    fun updateMember(memberKey: String, hashMap: HashMap<String, Any>) : Task<Void> {
        return memberDatabaseReference!!.child(memberKey).updateChildren(hashMap)
    }

    fun deleteShop(reserveKey: String): Task<Void> {
        return reservationDatabaseReference!!.child(reserveKey).removeValue()
    }
}
