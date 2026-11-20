package com.example.healthtrack.ui.tracking

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.*
import android.location.Location
import android.os.*
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.example.healthtrack.data.AppDatabase
import com.example.healthtrack.data.Trajet
import com.example.healthtrack.databinding.FragmentTrackingBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TrackingFragment : Fragment(), SensorEventListener, OnMapReadyCallback {

    private lateinit var binding: FragmentTrackingBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private lateinit var googleMap: GoogleMap
    private lateinit var locationCallback: LocationCallback

    private var isTracking = false
    private var stepCount = 0
    private var elapsedSeconds = 0L
    private val routePoints = mutableListOf<LatLng>()
    private var polyline: Polyline? = null
    private var lastLocation: Location? = null
    private var totalDistance = 0f

    // Accelerometer vars
    private var lastAcceleration = 0f
    private var currentAcceleration = 0f
    private var acceleration = 0f
    private var initialStepCount = -1
    private var lastStepTime = 0L
    private var stepThreshold = 3.0f

    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            elapsedSeconds++
            val min = elapsedSeconds / 60
            val sec = elapsedSeconds % 60
            binding.tvTimer.text = String.format("%02d:%02d", min, sec)
            timerHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        sensorManager = requireContext()
            .getSystemService(android.content.Context.SENSOR_SERVICE) as SensorManager

        // Demander permission ACTIVITY_RECOGNITION (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    200
                )
            }
        }

        val mapFragment = childFragmentManager
            .findFragmentById(com.example.healthtrack.R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnStartStop.setOnClickListener {
            if (!isTracking) startTracking() else stopTracking()
        }
        binding.btnSave.setOnClickListener { saveTrajet() }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            centerMapOnMyLocation()
        }
    }

    private fun centerMapOnMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val myPos = LatLng(it.latitude, it.longitude)
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(myPos, 16f)
                    )
                }
            }
        }
    }

    private fun startTracking() {
        isTracking = true
        stepCount = 0
        totalDistance = 0f
        elapsedSeconds = 0
        initialStepCount = -1
        lastAcceleration = 0f
        currentAcceleration = 0f
        acceleration = 0f
        lastStepTime = 0L
        routePoints.clear()

        binding.btnStartStop.text = "⏹  Arrêter le trajet"
        binding.btnStartStop.setBackgroundResource(
            com.example.healthtrack.R.drawable.bg_btn_red
        )
        binding.btnSave.visibility = View.GONE
        binding.tvTrackingStatus.text = "🔴 En cours..."

        timerHandler.postDelayed(timerRunnable, 1000)

        // Capteurs — essayer dans l'ordre
        val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        android.util.Log.d("SENSOR", "StepDetector: ${stepDetector != null}")
        android.util.Log.d("SENSOR", "StepCounter: ${stepCounter != null}")

        when {
            stepDetector != null -> {
                sensorManager.registerListener(
                    this, stepDetector, SensorManager.SENSOR_DELAY_FASTEST
                )
            }
            stepCounter != null -> {
                sensorManager.registerListener(
                    this, stepCounter, SensorManager.SENSOR_DELAY_FASTEST
                )
            }
            accelerometer != null -> {
                sensorManager.registerListener(
                    this, accelerometer, SensorManager.SENSOR_DELAY_GAME
                )
            }
        }

        // GPS
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 3000L
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val point = LatLng(loc.latitude, loc.longitude)
                routePoints.add(point)

                lastLocation?.let { prev ->
                    val res = FloatArray(1)
                    Location.distanceBetween(
                        prev.latitude, prev.longitude,
                        loc.latitude, loc.longitude, res
                    )
                    totalDistance += res[0]
                    binding.tvDistance.text =
                        String.format("%.2f km", totalDistance / 1000f)
                }
                lastLocation = loc

                polyline?.remove()
                polyline = googleMap.addPolyline(
                    PolylineOptions()
                        .addAll(routePoints)
                        .color(Color.parseColor("#818CF8"))
                        .width(8f)
                )
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(point, 17f)
                )
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                request, locationCallback, Looper.getMainLooper()
            )
        }
    }

    private fun stopTracking() {
        isTracking = false
        timerHandler.removeCallbacks(timerRunnable)
        sensorManager.unregisterListener(this)
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        binding.btnStartStop.text = "▶  Démarrer le trajet"
        binding.btnStartStop.setBackgroundResource(
            com.example.healthtrack.R.drawable.bg_btn_green
        )
        binding.tvTrackingStatus.text = "Terminé ✓"
        if (stepCount > 0 || totalDistance > 0f) {
            binding.btnSave.visibility = View.VISIBLE
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {

            Sensor.TYPE_STEP_DETECTOR -> {
                stepCount++
                updateStepsUI()
            }

            Sensor.TYPE_STEP_COUNTER -> {
                if (initialStepCount == -1) {
                    initialStepCount = event.values[0].toInt()
                }
                stepCount = event.values[0].toInt() - initialStepCount
                updateStepsUI()
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val magnitude = Math.sqrt(
                    (x * x + y * y + z * z).toDouble()
                ).toFloat()

                val diff = Math.abs(magnitude - 9.8f)

                val currentTime = System.currentTimeMillis()

                if (diff > stepThreshold &&
                    (currentTime - lastStepTime) > 250
                ) {
                    stepCount++
                    lastStepTime = currentTime
                    updateStepsUI()
                }
            }
        }
    }

    private fun updateStepsUI() {
        binding.tvSteps.text = stepCount.toString()
        binding.tvCalories.text = String.format("%.0f", stepCount * 0.04f)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun saveTrajet() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val date = SimpleDateFormat("dd/MM", Locale.FRENCH).format(Date())
            val trajet = Trajet(
                nom = "Trajet du $date",
                distance = totalDistance / 1000f,
                duree = elapsedSeconds,
                pas = stepCount,
                calories = stepCount * 0.04f,
                routeJson = Gson().toJson(routePoints)
            )
            db.healthDao().insertTrajet(trajet)
            binding.btnSave.visibility = View.GONE
            Toast.makeText(
                requireContext(),
                "✓ Trajet sauvegardé !",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerHandler.removeCallbacks(timerRunnable)
        if (isTracking) stopTracking()
    }
}