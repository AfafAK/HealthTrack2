package com.example.healthtrack.ui.history

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.example.healthtrack.data.AppDatabase
import com.example.healthtrack.data.Trajet
import com.example.healthtrack.databinding.FragmentHistoryBinding
import com.example.healthtrack.databinding.ItemTrajetBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TrajetAdapter()
        binding.recyclerTrajets.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTrajets.adapter = adapter

        lifecycleScope.launch {
            AppDatabase.getInstance(requireContext())
                .healthDao()
                .getAllTrajets()
                .collectLatest { list ->
                    binding.tvTrajetCount.text = "${list.size} trajet(s) enregistré(s)"
                    adapter.submitList(list)
                }
        }
    }
}

class TrajetAdapter : ListAdapter<Trajet, TrajetAdapter.VH>(
    object : DiffUtil.ItemCallback<Trajet>() {
        override fun areItemsTheSame(a: Trajet, b: Trajet) = a.id == b.id
        override fun areContentsTheSame(a: Trajet, b: Trajet) = a == b
    }
) {
    private var expandedId = -1

    inner class VH(val b: ItemTrajetBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTrajetBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = getItem(position)
        val sdf = SimpleDateFormat("dd MMM", Locale.FRENCH)
        holder.b.tvTrajetNom.text = t.nom
        holder.b.tvTrajetInfo.text =
            "${sdf.format(Date(t.date))} · ${t.duree / 60} min"
        holder.b.tvTrajetDistance.text = String.format("%.1f km", t.distance)
        holder.b.tvDetailPas.text = t.pas.toString()
        holder.b.tvDetailCalories.text = String.format("%.0f kcal", t.calories)

        val isExpanded = expandedId == t.id
        holder.b.layoutDetail.visibility =
            if (isExpanded) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            expandedId = if (isExpanded) -1 else t.id
            notifyDataSetChanged()
        }
    }
}
