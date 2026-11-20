package com.example.healthtrack

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.healthtrack.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            goToMain()
            return
        }

        binding.btnLogin.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val pass = binding.etPassword.text.toString()

            when {
                name.isEmpty() -> showError("Entrez votre nom")
                pass.isEmpty() -> showError("Entrez votre mot de passe")
                name == "Yassine" && pass == "1234" -> {
                    prefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putString("user_name", name)
                        .apply()
                    goToMain()
                }
                else -> showError("Nom ou mot de passe incorrect")
            }
        }
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.postDelayed({
            binding.tvError.visibility = View.GONE
        }, 2500)
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}