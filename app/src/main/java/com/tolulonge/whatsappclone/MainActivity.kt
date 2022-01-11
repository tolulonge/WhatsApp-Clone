package com.tolulonge.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.tolulonge.whatsappclone.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var myTabsAccessorAdapter: TabsAccessorAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

         setSupportActionBar(binding.mainToolBar)
        supportActionBar?.title = "WhatsApp"
        myTabsAccessorAdapter = TabsAccessorAdapter(supportFragmentManager, 3)
        binding.mainTabsPager.apply {
            adapter = myTabsAccessorAdapter
        }.also { binding.mainTabs.setupWithViewPager(it) }
    }
}