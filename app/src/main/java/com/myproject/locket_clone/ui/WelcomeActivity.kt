package com.myproject.locket_clone.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myproject.locket_clone.R
import com.myproject.locket_clone.databinding.ActivityWelcomeBinding
import com.myproject.locket_clone.ui.create_account.ChooseEmailActivity
import com.myproject.locket_clone.ui.login.LoginActivity

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateAccount.setOnClickListener {
            val intent = Intent(this, ChooseEmailActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}