
package com.example.matterdemosampleapp.ui.views.toggle.interfaces;


import com.example.matterdemosampleapp.ui.views.toggle.model.ToggleableView;

/**
 * Interface definition for a callback to be invoked when a digital switch is either on/off.
 *
 * <p>This is a <a href="package-summary.html">event listener</a>
 * whose event method is {@link #onSwitched(ToggleableView, boolean)}.
 *
 * @since 1.1.0
 */

public interface OnToggledListener {

    /**
     * Called when a views changes it's state.
     *
     * @param toggleableView The views which either is on/off.
     * @param isOn The on/off state of switch, true when switch turns on.
     */
    void onSwitched(ToggleableView toggleableView, boolean isOn);
}
