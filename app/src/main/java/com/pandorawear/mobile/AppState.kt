package com.pandorawear.mobile

enum class AppState {
    BACKEND_UNAVAILABLE,          // 1. нет конфига или бэк не пингуется
    BACKEND_AVAILABLE_NO_DEVICE,  // 2. бэк ок, но нет device/token
    BACKEND_READY_WITH_DEVICE,    // 3. бэк ок + есть device/token
}