package com.example.matterdemosampleapp

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.matterdemosampleapp.adapter.MatterDevicesAdapter
import com.example.matterdemosampleapp.databinding.ActivityMainBinding
import com.example.matterdemosampleapp.dto.MatterDevice
import com.example.matterdemosampleapp.service.AppCommissioningService
import com.google.android.gms.home.matter.Matter
import com.google.android.gms.home.matter.commissioning.CommissioningRequest
import com.google.android.gms.home.matter.commissioning.CommissioningResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var viewDataBinding: ActivityMainBinding
    private var deviceList = mutableListOf<MatterDevice>()

    private val TAG = ">>//"

    private lateinit var commissioningResult: ActivityResultLauncher<IntentSenderRequest>

    @Inject
    lateinit var matterDevicesAdapter: MatterDevicesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewDataBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewDataBinding.root)
        init()
        initViews()
    }

    private fun init() {
        setData()
    }

    private fun initViews() {
        setToolbarData()
    }

    private fun setToolbarData() {
        viewDataBinding.toolbar.tvTitle.text = "Home"
    }

    private fun setData() {
        commissioningResult = getCommissioningResult()

        viewDataBinding.listMatterDevices.adapter = matterDevicesAdapter

        viewDataBinding.btnAdd.setOnClickListener {
            commissionDevice()
        }
    }

    /**
     * Commision a Device
     * */
    private fun commissionDevice() {
        val commissionDeviceRequest = CommissioningRequest.builder()
            .setCommissioningService(ComponentName(this, AppCommissioningService::class.java))
            .build()

        // The call to commissionDevice() creates the IntentSender that will eventually be launched
        // in the fragment to trigger the commissioning activity in GPS.
        Matter.getCommissioningClient(this).commissionDevice(commissionDeviceRequest)
            .addOnSuccessListener { result ->
                Log.d(TAG, "Commissioning Device Success")
                commissioningResult.launch(IntentSenderRequest.Builder(result).build())
            }.addOnFailureListener { error ->
                Log.d(TAG, "Commissioning Device Failure -> ${error.message}")
                //      _commissionDeviceStatus.postValue(
                //        TaskStatus.Failed("Setting up the IntentSender failed", error))
            }
    }

    private fun getCommissioningResult(): ActivityResultLauncher<IntentSenderRequest> {
        return registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "Commissioning Result Success")
                addDeviceToList(result)
            } else {
                Log.d(TAG, "Commissioning Result Failure")
            }
        }
    }

    private fun addDeviceToList(result: ActivityResult) {
        val commissioningResult =
            CommissioningResult.fromIntentSenderResult(result.resultCode, result.data)
        deviceList.add(
            MatterDevice(
                vendorId = commissioningResult.commissionedDeviceDescriptor.vendorId,
                productId = commissioningResult.commissionedDeviceDescriptor.productId,
                name = commissioningResult.deviceName,
                room = commissioningResult.room?.name,
                deviceId = commissioningResult.token
            )
        )
        setAdapterData()
    }

    private fun setAdapterData(){
        matterDevicesAdapter.setData(deviceList)
    }


}