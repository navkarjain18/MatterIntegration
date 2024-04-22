package com.example.matterdemosampleapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.matterdemosampleapp.R
import com.example.matterdemosampleapp.databinding.RowMatterDeviceItemBinding
import com.example.matterdemosampleapp.dto.MatterDevice
import com.example.matterdemosampleapp.listeneres.OnViewClickListener
import javax.inject.Inject

class MatterDevicesAdapter @Inject constructor() :
    RecyclerView.Adapter<MatterDevicesAdapter.ViewHolder>() {

    var list = listOf<MatterDevice?>()
    private var itemClickListener: OnViewClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: RowMatterDeviceItemBinding = DataBindingUtil.inflate(
            layoutInflater, R.layout.row_matter_device_item, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: RowMatterDeviceItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setView(position, holder.binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if(payloads.isNotEmpty()){
            holder.binding.labeledSwitch.isOn = payloads[0] as Boolean
        } else super.onBindViewHolder(holder, position, payloads)
    }

    private fun setView(
        position: Int, binding: RowMatterDeviceItemBinding
    ) {
        val matterDevice = list[position]
        binding.tvName.text = matterDevice?.name

        binding.labeledSwitch.isOn = matterDevice?.isOn == true

        binding.labeledSwitch.setOnToggledListener { toggleableView, isOn ->
            list[position]?.isOn = isOn
            itemClickListener?.onItemClick(toggleableView, matterDevice)
        }

        binding.root.setOnClickListener {
            itemClickListener?.onItemLongClick(it ,matterDevice)
        }
    }

    /**
     * Set Data
     * */
    fun setData(list: List<MatterDevice?>) {
        this.list = list
        notifyDataSetChanged()
    }

    /**
     * Set Item Click Listener
     * */
    fun setItemClick(itemClickListener: OnViewClickListener) {
        this.itemClickListener = itemClickListener
    }
}