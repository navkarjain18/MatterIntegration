package com.example.matterdemosampleapp.ui.views.toggle.interfaces;

import android.view.View;

/**
 * Interface definition for a callback to be invoked when state of switch is changed.
 *
 * <p>This is a <a href="package-summary.html">event listener</a>
 * whose event method is {@link #onStateChanged(View, int)}.
 *
 * @since 1.1.0
 */
public interface OnStateChangedListener {

    /**
     * Called when a views changes it's state.
     *
     * @param view The views whose state was changed.
     * @param state The state of the views.
     */
    void onStateChanged(View view, int state);
}
