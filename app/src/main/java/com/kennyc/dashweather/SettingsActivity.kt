package com.kennyc.dashweather

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * Created by kcampagna on 10/6/17.
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.
                beginTransaction()
                .add(android.R.id.content, SettingsFragment())
                .commit()

    }
}