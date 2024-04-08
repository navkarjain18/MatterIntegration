package com.example.matterdemosampleapp

enum class CommissioningWindowStatus(val status: Int) {
    /** Commissioning window not open */
    WindowNotOpen(0),

    /** An Enhanced Commissioning Method window is open */
    EnhancedWindowOpen(1),

    /** A Basic Commissioning Method window is open */
    BasicWindowOpen(2)
}

enum class DeviceType(val value: Int) {
    TYPE_UNSPECIFIED(0),
    TYPE_UNKNOWN(1),
    TYPE_LIGHT(2),
    TYPE_OUTLET(3),
    TYPE_DIMMABLE_LIGHT(4),
    TYPE_COLOR_TEMPERATURE_LIGHT(5),
    TYPE_EXTENDED_COLOR_LIGHT(6),
    TYPE_LIGHT_SWITCH(7),
    UNRECOGNIZED(-1);
}