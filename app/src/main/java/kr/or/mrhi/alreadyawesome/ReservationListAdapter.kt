package kr.or.mrhi.alreadyawesome

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import kr.or.mrhi.alreadyawesome.databinding.ItemReservationListBinding

class ReservationListAdapter(val context: Context, val reservationList: MutableList<Reservation>?) : RecyclerView.Adapter<ReservationListAdapter.CustomViewHolder>() {
    override fun getItemCount(): Int {
        return reservationList?.size?:0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemReservationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        val reservation = reservationList?.get(position)
        // reservation 안에 들어있는 shopId와 memberId값을 기준으로 shop과 member 데이터를 가져옴
        val dbHelper = DBHelper(context, MainActivity.DB_NAME, MainActivity.VERSION)
        val shopList = dbHelper.selectShopById(reservation?.shopId)
        val memberList = dbHelper.selectMemberById(reservation?.memberId)
        val selectShop = shopList?.get(0)
        val selectMember = memberList?.get(0)

        binding.tvListShopName.text = selectShop?.shopName
        binding.tvListDate.text = reservation?.reserveDate
        binding.tvListMenu.text = reservation?.reserveMenu
        binding.tvListTime.text = reservation?.reserveTime
        binding.tvListName.text = selectMember?.memberName
        binding.tvListAddress.text = selectShop?.address
        binding.tvListPhone.text = selectShop?.shopPhone
        binding.tvListPrice.text = reservation?.price
        binding.tvListPayment.text = reservation?.payment

        // 예약취소 버튼을 클릭시 해당 데이터를 삭제
        binding.btnListCancel.setOnClickListener {
            (context as ReservationListActivity).removeReservation(position, reservation)
        }
    }

    class CustomViewHolder(val binding: ItemReservationListBinding) : RecyclerView.ViewHolder(binding.root)
}