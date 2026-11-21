package com.student.qujian

import android.app.Application
import com.student.qujian.core.PickupStore

class QujianApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PickupStore.init(this)
    }
}
