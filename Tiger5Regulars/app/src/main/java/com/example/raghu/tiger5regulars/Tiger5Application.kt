package com.example.raghu.tiger5regulars

import android.app.Application
import androidx.preference.PreferenceManager
import com.example.raghu.tiger5regulars.utilities.ThemeHelper

class Tiger5Application : Application() {

    override fun onCreate() {
        super.onCreate()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePref = sharedPreferences.getString("themePref", ThemeHelper.DEFAULT_MODE)
        if (themePref != null) {
            ThemeHelper.applyTheme(themePref)
        }
    }
}