package com.example.whatsappclone

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class screenpager(f: FragmentActivity):FragmentStateAdapter(f) {
    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun createFragment(position: Int): Fragment {
       return  when(position){
            0->Inboxfr()
            else->peoplefr()
        }
    }
}