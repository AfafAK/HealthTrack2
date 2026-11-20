package com.example.healthtrack.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.example.healthtrack.R
import com.example.healthtrack.databinding.FragmentStatsBinding

class StatsFragment : Fragment() {

    private lateinit var binding: FragmentStatsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupStepsChart()
        setupCaloriesChart()
        setupImcChart()
        setupTensionChart()
    }

    private fun setupTabs() {
        val panels = listOf(binding.panelActivite, binding.panelImc, binding.panelTension)
        val tabs = listOf(binding.tabActivite, binding.tabImc, binding.tabTension)

        fun selectTab(index: Int) {
            panels.forEachIndexed { i, p ->
                p.visibility = if (i == index) View.VISIBLE else View.GONE
            }
            tabs.forEachIndexed { i, t ->
                t.setBackgroundResource(
                    if (i == index) R.drawable.bg_btn_primary else R.drawable.bg_card
                )
                t.setTextColor(
                    if (i == index) Color.WHITE else Color.parseColor("#88FFFFFF")
                )
            }
        }

        binding.tabActivite.setOnClickListener { selectTab(0) }
        binding.tabImc.setOnClickListener { selectTab(1) }
        binding.tabTension.setOnClickListener { selectTab(2) }
    }

    private fun setupStepsChart() {
        val entries = listOf(4200f, 7800f, 6100f, 9300f, 5600f, 11200f, 8900f)
            .mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val dataset = BarDataSet(entries, "Pas").apply {
            color = Color.parseColor("#818CF8")
            valueTextColor = Color.TRANSPARENT
        }
        binding.chartSteps.apply {
            data = BarData(dataset)
            description.isEnabled = false
            legend.isEnabled = false
            setBackgroundColor(Color.TRANSPARENT)
            xAxis.apply {
                textColor = Color.GRAY
                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(
                    listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
                )
            }
            axisLeft.textColor = Color.GRAY
            axisRight.isEnabled = false
            animateY(1000)
        }
    }

    private fun setupCaloriesChart() {
        val entries = listOf(180f, 320f, 250f, 390f, 230f, 470f, 370f)
            .mapIndexed { i, v -> Entry(i.toFloat(), v) }
        val dataset = LineDataSet(entries, "Calories").apply {
            color = Color.parseColor("#F472B6")
            setCircleColor(Color.parseColor("#F472B6"))
            lineWidth = 2f
            circleRadius = 3f
            setDrawFilled(true)
            fillColor = Color.parseColor("#F472B6")
            fillAlpha = 40
            valueTextColor = Color.TRANSPARENT
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        binding.chartCalories.apply {
            data = LineData(dataset)
            description.isEnabled = false
            legend.isEnabled = false
            setBackgroundColor(Color.TRANSPARENT)
            xAxis.apply {
                textColor = Color.GRAY
                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(
                    listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
                )
            }
            axisLeft.textColor = Color.GRAY
            axisRight.isEnabled = false
            animateX(1000)
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
        binding.chartImc.apply {
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
            axisLeft.apply {
                textColor = Color.GRAY
                axisMinimum = 22f
                axisMaximum = 30f
            }
            axisRight.isEnabled = false
            animateX(1000)
        }
    }

    private fun setupTensionChart() {
        val sys = listOf(128f, 125f, 130f, 122f, 119f)
            .mapIndexed { i, v -> Entry(i.toFloat(), v) }
        val dia = listOf(82f, 80f, 85f, 79f, 77f)
            .mapIndexed { i, v -> Entry(i.toFloat(), v) }

        val d1 = LineDataSet(sys, "Systolique").apply {
            color = Color.parseColor("#F87171")
            setCircleColor(Color.parseColor("#F87171"))
            lineWidth = 2.5f
            circleRadius = 4f
            valueTextColor = Color.TRANSPARENT
        }
        val d2 = LineDataSet(dia, "Diastolique").apply {
            color = Color.parseColor("#60A5FA")
            setCircleColor(Color.parseColor("#60A5FA"))
            lineWidth = 2.5f
            circleRadius = 4f
            valueTextColor = Color.TRANSPARENT
        }
        binding.chartTension.apply {
            data = LineData(d1, d2)
            description.isEnabled = false
            legend.apply {
                isEnabled = true
                textColor = Color.GRAY
            }
            setBackgroundColor(Color.TRANSPARENT)
            xAxis.apply {
                textColor = Color.GRAY
                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(
                    listOf("01/04", "05/04", "10/04", "15/04", "18/04")
                )
            }
            axisLeft.apply {
                textColor = Color.GRAY
                axisMinimum = 60f
                axisMaximum = 145f
            }
            axisRight.isEnabled = false
            animateX(1000)
        }
    }
}