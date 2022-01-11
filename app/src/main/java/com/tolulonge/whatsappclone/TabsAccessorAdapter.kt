package com.tolulonge.whatsappclone

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TabsAccessorAdapter(private val fm : FragmentManager, private val count : Int) : FragmentPagerAdapter(fm, count) {
    override fun getCount(): Int {
        return count
    }

    override fun getItem(position: Int): Fragment {
      return  when(position){
            0 ->  ChatsFragment()
            1 ->   GroupsFragment()
            2 ->   ContactsFragment()
          else -> ChatsFragment()
      }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return  when(position){
            0 ->  "Chats"
            1 ->   "Groups"
            2 ->   "Contacts"
            else -> null
        }
    }
}