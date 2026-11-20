package com.example.healthtrack.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.example.healthtrack.R
import com.example.healthtrack.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("user_prefs", 0)
        val name = prefs.getString("user_name", "Utilisateur") ?: "Utilisateur"
        val poids = prefs.getFloat("poids", 0f)
        val taille = prefs.getFloat("taille", 0f)
        val tension = prefs.getString("tension", "--") ?: "--"

        binding.tvUserName.text = name
        binding.tvDate.text = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRENCH)
            .format(Date()).replaceFirstChar { it.uppercase() }

        if (poids > 0f && taille > 0f) {
            val imc = poids / ((taille / 100f) * (taille / 100f))
            binding.tvImcValue.text = String.format("%.1f", imc)
            binding.tvPoidsHome.text = "${poids.toInt()} kg"
            binding.tvTailleHome.text = "${taille.toInt()} cm"
            binding.tvTensionHome.text = tension

            val (label, color) = when {
                imc < 18.5f -> "Insuffisance pondérale" to "#60A5FA"
                imc < 25f   -> "Poids normal ✓" to "#4ADE80"
                imc < 30f   -> "Surpoids" to "#FBBF24"
                else        -> "Obésité" to "#F87171"
            }
            binding.tvImcCategory.text = label
            binding.tvImcCategory.setTextColor(Color.parseColor(color))
        } else {
            binding.tvImcValue.text = "--"
            binding.tvImcCategory.text = "Complétez votre profil"
            binding.tvImcCategory.setTextColor(Color.parseColor("#88FFFFFF"))
        }

        setupImcChart()

        binding.btnStartTrajet.setOnClickListener {
            findNavController().navigate(R.id.trackingFragment)
        }
        binding.btnGoStats.setOnClickListener {
            findNavController().navigate(R.id.statsFragment)
        }
        binding.btnGoHistory.setOnClickListener {
            findNavController().navigate(R.id.historyFragment)
        }
    }

    private fun setupImcChart() {
        val entries = listOf(27.2f, 26.8f, 26.1f, 25.5f, 24.9f, 24.3f)
            .mapIndexed { i, v -> Entry(i.toFloat(), v) }

        val dataset = LineDataSet(entries, "IMC").apply {
            color = Color.parseColor("#818CF8")
            setCircleColor(Color.parseColor("#818CF8"))
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawFilled(true)
            fillColor = Color.parseColor("#818CF8")
            fillAlpha = 40
            valueTextColor = Color.TRANSPARENT
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.chartImcHome.apply {
            data = LineData(dataset)
            description.isEnabled = false
            legend.isEnabled = false
            setBackgroundColor(Color.TRANSPARENT)
            xAxis.apply {
                textColor = Color.GRAY
                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(
                    listOf("Jan", "Fév", "Mar", "Avr", "Mai", "Jun")
                )
            }
            axisLeft.textColor = Color.GRAY
            axisRight.isEnabled = false
            animateX(1000)
        }
    }
}