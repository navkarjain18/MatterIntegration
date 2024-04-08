package com.example.matterdemosampleapp

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import chip.devicecontroller.model.NodeState
import com.example.matterdemosampleapp.adapter.MatterDevicesAdapter
import com.example.matterdemosampleapp.chip.ChipClient
import com.example.matterdemosampleapp.chip.ClustersHelper
import com.example.matterdemosampleapp.chip.MatterConstants.OnOffAttribute
import com.example.matterdemosampleapp.chip.SubscriptionHelper
import com.example.matterdemosampleapp.databinding.ActivityMainBinding
import com.example.matterdemosampleapp.dto.MatterDevice
import com.example.matterdemosampleapp.dto.MatterDevices
import com.example.matterdemosampleapp.listeneres.OnViewClickListener
import com.example.matterdemosampleapp.local.DataPreferenceKeys
import com.example.matterdemosampleapp.local.DataStorePreference
import com.example.matterdemosampleapp.service.AppCommissioningService
import com.example.matterdemosampleapp.utils.AppUtils.convertToAppDeviceType
import com.example.matterdemosampleapp.utils.encode
import com.google.android.gms.home.matter.Matter
import com.google.android.gms.home.matter.commissioning.CommissioningRequest
import com.google.android.gms.home.matter.commissioning.CommissioningResult
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var viewDataBinding: ActivityMainBinding
    private var deviceList = mutableListOf<MatterDevice>()

    private val TAG = ">>//"

    private lateinit var commissioningResult: ActivityResultLauncher<IntentSenderRequest>

    @Inject
    lateinit var matterDevicesAdapter: MatterDevicesAdapter

    @Inject
    lateinit var clustersHelper: ClustersHelper

    @Inject
    lateinit var chipClient: ChipClient

    @Inject
    lateinit var subscriptionHelper: SubscriptionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewDataBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewDataBinding.root)
        init()
        initViews()
    }

    private fun init() {
        getData()
        setData()
        setAdapterData()
    }

    private fun initViews() {
        setToolbarData()
    }

    private fun setToolbarData() {
        viewDataBinding.toolbar.btnEdit.visibility = View.VISIBLE
        viewDataBinding.toolbar.tvTitle.text = "Home"
        viewDataBinding.toolbar.btnEdit.text = "Reset"
    }

    private fun getData() {
        deviceList = runBlocking {
             Gson().fromJson(
                DataStorePreference.getFirstPreference(DataPreferenceKeys.MATTER_DEVICES_LIST, ""),
                MatterDevices::class.java
            )?.matterDeviceList.orEmpty().toMutableList()
        }
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
                Log.e(TAG, "Commissioning Device Failure -> ${error.message}")
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
                deviceId = commissioningResult.token?.toLong(),
                deviceType = convertToAppDeviceType(commissioningResult.commissionedDeviceDescriptor.deviceType.toLong())
            )
        )
        runBlocking {
            DataStorePreference.addPreference(
                DataPreferenceKeys.MATTER_DEVICES_LIST,
                MatterDevices(deviceList).encode(MatterDevices::class.java)
            )
        }
        setAdapterData()
    }

    private fun setAdapterData() {
        matterDevicesAdapter.setData(deviceList)

        matterDevicesAdapter.setItemClick(object : OnViewClickListener {
            override fun <T> onItemClick(view: View, data: T) {
                super.onItemClick(view, data)
                val matterDevice = data as? MatterDevice

                CoroutineScope(Dispatchers.IO).launch {
                    clustersHelper.setOnOffDeviceStateOnOffCluster(
                        matterDevice?.deviceId ?: 0L, matterDevice?.isOn == true, 1
                    )
                }
            }

        })

        subscribeToDevicesPeriodicUpdates()
    }

    private fun reset() {

    }

    private fun subscribeToDevicesPeriodicUpdates() {
        Log.d(TAG, "subscribeToDevicesPeriodicUpdates()")
        CoroutineScope(Dispatchers.IO).launch {
            // For each one of the real devices
            deviceList.forEachIndexed { index, device ->
                val reportCallback =
                    object : SubscriptionHelper.ReportCallbackForDevice(device.deviceId ?: 0) {
                        override fun onReport(nodeState: NodeState) {
                            super.onReport(nodeState)
                            // TODO: See HomeViewModel:CommissionDeviceSucceeded for device capabilities
                            val onOffState = subscriptionHelper.extractAttribute(
                                nodeState, 1, OnOffAttribute
                            ) as Boolean?
                            Log.d(TAG, "Response onOffState [${onOffState}]")
                            if (onOffState == null) {
                                Log.e(TAG, "onReport(): WARNING -> onOffState is NULL. Ignoring.")
                                return
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                matterDevicesAdapter.notifyItemChanged(index, onOffState)
                            }

                        }
                    }

                try {
                    val connectedDevicePointer =
                        chipClient.getConnectedDevicePointer(device.deviceId ?: 0)
                    subscriptionHelper.awaitSubscribeToPeriodicUpdates(
                        connectedDevicePointer,
                        SubscriptionHelper.SubscriptionEstablishedCallbackForDevice(
                            device.deviceId ?: 0
                        ),
                        SubscriptionHelper.ResubscriptionAttemptCallbackForDevice(
                            device.deviceId ?: 0
                        ),
                        reportCallback,
                    )
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "Can't get connectedDevicePointer for ${device.deviceId}.")
                    return@forEachIndexed
                }
            }
        }
    }


}