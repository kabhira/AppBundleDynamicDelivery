package com.example.interfacefeature

import android.content.Context
import android.widget.Toast
import com.example.myapplication.TestInterface

class TestInterfaceImpl : TestInterface {
    override fun callMe(context: Context) {
        Toast.makeText(context, "Called from TestInterfaceImpl.", Toast.LENGTH_SHORT).show()
    }

}