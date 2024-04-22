/*
 * Copyright 2023 DigiValet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.matterdemosampleapp.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.matterdemosampleapp.R
import com.example.matterdemosampleapp.chip.ClustersHelper
import com.example.matterdemosampleapp.databinding.DialogLightControlsBinding
import com.example.matterdemosampleapp.dto.MatterDevice
import com.example.matterdemosampleapp.ui.views.ProgressListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class LightControlDialog(private val matterDevice: MatterDevice?) : DialogFragment() {

    private var minValue: Int? = 0
    private var maxValue: Int? = 100
    private var currentValue: Int? = 100

    private var ratio = 0f

    private lateinit var binding: DialogLightControlsBinding

    @Inject
    lateinit var clustersHelper: ClustersHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme_FullScreenDialog)
    }


    fun display(fragmentManager: FragmentManager): LightControlDialog {
        val dialog = LightControlDialog(matterDevice)
        dialog.show(fragmentManager, "")
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_light_controls, container, false
        )
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initViews()
    }

    private fun init() {
        CoroutineScope(Dispatchers.IO).launch {
            minValue = 0
            maxValue = 100

            try {
                currentValue = clustersHelper.getCurrentLevel(matterDevice?.deviceId ?: 0, 1)
                ratio = (clustersHelper.getMaxLevel(matterDevice?.deviceId ?: 0, 1) ?: 100) / 100f
                withContext(Dispatchers.Main) {
                    controlLight()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun initViews() {
        binding.btnClose.setOnClickListener { dismissAllowingStateLoss() }
        binding.tvLightName.text = matterDevice?.name
    }

    private fun controlLight() {
        binding.brightnessSlider.setMinProgress(minValue ?: 0)
        binding.brightnessSlider.setMaxProgress(maxValue ?: 100)
        binding.brightnessSlider.setCurrentProgress(((currentValue ?: 0) / ratio).toInt())
        binding.tvBrightnessValue.text = "${((currentValue ?: 0) / ratio).roundToInt()}%"

        binding.brightnessSlider.setProgressListener(object : ProgressListener {
            override fun beforeProgressChange(progress: Int) { /*not implemented*/
            }

            override fun onProgressChange(progress: Int) {
                binding.tvBrightnessValue.text = "${progress}%"
            }

            override fun afterProgressChange(progress: Int) {
                setProgress(progress)
            }
        })
    }

    private fun setProgress(progress: Int) {
        CoroutineScope(Dispatchers.IO).launch {

            try {
                clustersHelper.setLevelClusterLevel(
                    matterDevice?.deviceId ?: 0, 1, ((progress * ratio).toInt())
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

}