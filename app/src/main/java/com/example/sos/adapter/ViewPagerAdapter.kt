package com.example.sos.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
    private var mFragmentList = ArrayList<Fragment>()
    var mFragmentTitleList = ArrayList<String>()

    override fun getItemCount(): Int {
        return mFragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        /*return when(position){
            0 -> {
                TransactionFragment()
            }
            1 -> {
                DetailedStatementFragment()
            }
            2-> {
                ThirdFragment()
            }
            else -> {
                Fragment()
            }
        }*/
        return mFragmentList[position]
    }

    fun addFragment(fragment: Fragment, fragmentTitle: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(fragmentTitle)
    }
}