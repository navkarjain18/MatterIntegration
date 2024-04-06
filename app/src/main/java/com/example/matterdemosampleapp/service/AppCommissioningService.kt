package com.example.matterdemosampleapp.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.home.matter.commissioning.CommissioningCompleteMetadata
import com.google.android.gms.home.matter.commissioning.CommissioningRequestMetadata
import com.google.android.gms.home.matter.commissioning.CommissioningService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * The CommissioningService that's responsible for commissioning the device on the app's custom
 * fabric. AppCommissioningService is specified when building the
 * [com.google.android.gms.home.matter.commissioning.CommissioningRequest] in
 * [../screens.home.HomeViewModel].
 */
@AndroidEntryPoint
class AppCommissioningService : Service(), CommissioningService.Callback {

//    @Inject
//    internal lateinit var devicesRepository: DevicesRepository
//    @Inject
//    internal lateinit var devicesStateRepository: DevicesStateRepository
//    @Inject
//    internal lateinit var chipClient: ChipClient

    private val TAG = ">>//"

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)



    private lateinit var commissioningServiceDelegate: CommissioningService

    override fun onCreate() {
        super.onCreate()
        // May be invoked without MainActivity being called to initialize APP_NAME.
        // So do it here as well.
        Log.d(TAG , "OnCreateService")
        commissioningServiceDelegate = CommissioningService.Builder(this).setCallback(this).build()
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG , "onBind(): intent [${intent}]")
        return commissioningServiceDelegate.asBinder()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG , "onStartCommand(): intent [${intent}] flags [${flags}] startId [${startId}]")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG , "onDestroy()")
        serviceJob.cancel()
    }

    override fun onCommissioningRequested(metadata: CommissioningRequestMetadata) {
        Log.d(TAG ,
            "*** onCommissioningRequested ***:\n" +
                    "\tdeviceDescriptor: " +
                    "deviceType [${metadata.deviceDescriptor.deviceType}] " +
                    "vendorId [${metadata.deviceDescriptor.vendorId}] " +
                    "productId [${metadata.deviceDescriptor.productId}]\n" +
                    "\tnetworkLocation: " +
                    "IP address toString() [${metadata.networkLocation.ipAddress}] " +
                    "IP address hostAddress [${metadata.networkLocation.ipAddress.hostAddress}] " +
                    "port [${metadata.networkLocation.port}]\n" +
                    "\tpassCode [${metadata.passcode}]")

        // Perform commissioning on custom fabric for the sample app.
        serviceScope.launch {
//            val deviceId = getNextDeviceId(DeviceIdGenerator.Random)
            val deviceId = "10001"
            try {
                Log.d(TAG ,
                    "Commissioning: App fabric -> ChipClient.establishPaseConnection(): deviceId [${deviceId}]")
//                chipClient.awaitEstablishPaseConnection(
//                    deviceId,
//                    metadata.networkLocation.ipAddress.hostAddress!!,
//                    metadata.networkLocation.port,
//                    metadata.passcode)

                Log.d(TAG ,
                    "Commissioning: App fabric -> ChipClient.commissionDevice(): deviceId [${deviceId}]")
//                chipClient.awaitCommissionDevice(deviceId, null)
            } catch (e: Exception) {
                Log.e(TAG ,  "onCommissioningRequested() failed")
                // No way to determine whether this was ATTESTATION_FAILED or DEVICE_UNREACHABLE.
                commissioningServiceDelegate
                    .sendCommissioningError(CommissioningService.CommissioningError.OTHER)
                    .addOnSuccessListener {
                        Log.d(TAG ,
                            "Commissioning: commissioningServiceDelegate.sendCommissioningError() succeeded")
                    }
                    .addOnFailureListener { e2 ->
                        Log.e(TAG , "Commissioning: commissioningServiceDelegate.sendCommissioningError() failed")
                    }
                return@launch
            }

            Log.d(TAG ,"Commissioning: Calling commissioningServiceDelegate.sendCommissioningComplete()")
            commissioningServiceDelegate
                .sendCommissioningComplete(
                    CommissioningCompleteMetadata.builder().setToken(deviceId.toString()).build())
                .addOnSuccessListener {
                    Log.d(TAG ,
                        "Commissioning: commissioningServiceDelegate.sendCommissioningComplete() succeeded")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG ,"Commissioning: commissioningServiceDelegate.sendCommissioningComplete() failed")
                }
        }
    }

    /**
     * Generates the device id for the device being commissioned ToDo() move this function into an
     * appropriate class to make it visible in HomeFragmentRecyclerViewTest
     *
     * @param generator the method used to generate the device id
     */
    /*private suspend fun getNextDeviceId(generator: DeviceIdGenerator): Long {
        return when (generator) {
            DeviceIdGenerator.Incremental -> {
                devicesRepository.incrementAndReturnLastDeviceId()
            }
            DeviceIdGenerator.Random -> {
                generateNextDeviceId()
            }
        }
    }*/
}