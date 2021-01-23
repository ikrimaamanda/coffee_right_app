package com.istekno.coffeebreakapp.main.mainpage

import android.os.Bundle
import android.view.WindowManager
import com.istekno.coffeebreakapp.R
import com.istekno.coffeebreakapp.base.BaseActivity
import com.istekno.coffeebreakapp.databinding.ActivityMainPageBinding
import com.istekno.coffeebreakapp.main.login.LoginActivity
import com.istekno.coffeebreakapp.main.signup.SignupActivity

class MainPageActivity : BaseActivity<ActivityMainPageBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setLayout = R.layout.activity_main_page
        super.onCreate(savedInstanceState)

        window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )

        viewListener()
    }

    private fun viewListener() {
        binding.btnCreateAccount.setOnClickListener {
            intent<SignupActivity>(this)
        }
        binding.btnLogin.setOnClickListener {
            intent<LoginActivity>(this)
        }
    }
}