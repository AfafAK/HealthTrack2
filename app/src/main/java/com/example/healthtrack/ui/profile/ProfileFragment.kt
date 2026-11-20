package com.example.healthtrack.ui.profile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import com.example.healthtrack.LoginActivity
import com.example.healthtrack.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("user_prefs", 0)
        val name = prefs.getString("user_name", "U") ?: "U"

        binding.tvProfileName.text = name
        binding.tvAvatar.text = name.first().uppercase()

        val poids = prefs.getFloat("poids", 0f)
        val taille = prefs.getFloat("taille", 0f)
        val tension = prefs.getString("tension", "") ?: ""

        if (poids > 0f) binding.etPoids.setText(poids.toInt().toString())
        if (taille > 0f) binding.etTaille.setText(taille.toInt().toString())
        if (tension.isNotEmpty()) binding.etTension.setText(tension)

        updateImcDisplay(poids, taille)

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val p = binding.etPoids.text.toString().toFloatOrNull() ?: 0f
                val t = binding.etTaille.text.toString().toFloatOrNull() ?: 0f
                updateImcDisplay(p, t)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.etPoids.addTextChangedListener(watcher)
        binding.etTaille.addTextChangedListener(watcher)

        binding.btnSaveProfile.setOnClickListener {
            val p = binding.etPoids.text.toString().toFloatOrNull() ?: 0f
            val t = binding.etTaille.text.toString().toFloatOrNull() ?: 0f
            val tens = binding.etTension.text.toString()

            prefs.edit()
                .putFloat("poids", p)
                .putFloat("taille", t)
                .putString("tension", tens)
                .apply()

            binding.tvSaveSuccess.visibility = View.VISIBLE
            binding.tvSaveSuccess.postDelayed({
                binding.tvSaveSuccess.visibility = View.GONE
            }, 2500)
        }

        binding.btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun updateImcDisplay(poids: Float, taille: Float) {
        if (poids > 0f && taille > 0f) {
            val imc = poids / ((taille / 100f) * (taille / 100f))
            binding.tvImcProfile.text = String.format("%.1f", imc)
            val (label, color) = when {
                imc < 18.5f -> "Insuffisance" to "#60A5FA"
                imc < 25f   -> "Normal ✓" to "#4ADE80"
                imc < 30f   -> "Surpoids" to "#FBBF24"
                else        -> "Obésité" to "#F87171"
            }
            binding.tvImcCatProfile.text = label
            binding.tvImcCatProfile.setTextColor(Color.parseColor(color))
        }
    }
}