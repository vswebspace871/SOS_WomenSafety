package com.example.sos.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedDispatcher
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.example.sos.R
import com.example.sos.adapter.ViewPagerAdapter
import com.example.sos.databinding.ActivitySettingsBinding
import com.example.sos.fragments.ContactsFragment
import com.example.sos.fragments.LocationFragment
import com.example.sos.fragments.PersonalFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)

        viewPager = binding.pager

        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.addCallback()
        }

        /** this is not working from xml or theme style, it is working only programatically
         * Tabs will take full width NOW SUCCESSFULLY
         * */
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL;
        binding.tabLayout.tabMode = TabLayout.MODE_FIXED;


        /** Attaching Viewpager2 with TabLayout*/
        val viewPagerAdapter = ViewPagerAdapter(this@SettingsActivity)
        viewPagerAdapter.addFragment(PersonalFragment(), "PERSONAL")
        viewPagerAdapter.addFragment(ContactsFragment(), "CONTACTS")
        viewPagerAdapter.addFragment(LocationFragment(), "LOCATION")

        viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = viewPagerAdapter.mFragmentTitleList[position]
        }.attach()
    }

    private fun OnBackPressedDispatcher.addCallback() {
        onBackPressed()
    }
}