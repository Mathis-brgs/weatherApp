package com.example.weatherapp
import androidx.activity.result.contract.ActivityResultContracts

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.network.RetrofitInstance
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val apiKey = "HE7fxUmXbkgtPz844KcQ3bssF2rBa07T"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Utilisation du XML au lieu de Jetpack Compose

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Récupération des TextView du layout
        val tvLocation = findViewById<TextView>(R.id.tv_location)
        val tvTemperature = findViewById<TextView>(R.id.tv_temperature)


        checkLocationPermission { latitude, longitude ->
            fetchWeather(latitude, longitude) { temp, city, description ->
                runOnUiThread {
                    tvLocation.text = city
                    tvTemperature.text = temp
                    findViewById<TextView>(R.id.tv_weatherDescription).text = description
                }
            }
        }

    }

    private fun checkLocationPermission(onLocationAvailable: (Double, Double) -> Unit) {
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (isPermissionGranted) {
            getCurrentLocation(onLocationAvailable)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation { latitude, longitude ->
                fetchWeather(latitude, longitude) { _, _,_ -> }
            }
        } else {
            Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentLocation(onLocationAvailable: (Double, Double) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permission non accordée", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocationAvailable(location.latitude, location.longitude)
                } else {
                    Toast.makeText(this, "Impossible d'obtenir la localisation", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchWeather(
        latitude: Double,
        longitude: Double,
        onWeatherFetched: (String, String, String) -> Unit
    ) {
        val location = "$latitude,$longitude"
        val url = "https://api.tomorrow.io/v4/timelines?location=$location&fields=temperature,weatherCode&timesteps=1h&units=metric&apikey=$apiKey"


        Log.d("WeatherApp", "URL API: $url") // 🔍 Voir l'URL appelée

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                val addressList = geocoder.getFromLocation(latitude, longitude, 1)
                val city = addressList?.firstOrNull()?.locality ?: "Ville inconnue"

                val response = RetrofitInstance.api.getWeather(location = location, apiKey = apiKey) // ✅ Bien déclarée ici !

                Log.d("WeatherApp", "Réponse API : $response") // 🔍 Voir la réponse API

                val firstTimeline = response.data.timelines.firstOrNull()
                val firstInterval = firstTimeline?.intervals?.firstOrNull()

                if (firstInterval != null) {
                    val temperature = firstInterval.values.temperature
                    val weatherCode = firstInterval.values.weatherCode

                    Log.d("WeatherApp", "Température: $temperature, Code météo: $weatherCode") // 🔍 Vérifier les valeurs

                    val weatherDescription = getWeatherDescription(weatherCode)

                    runOnUiThread {
                        onWeatherFetched("$temperature°C", city, weatherDescription)
                    }
                } else {
                    Log.e("WeatherApp", "Données indisponibles")
                    runOnUiThread {
                        onWeatherFetched("Erreur", "Inconnue", "Données indisponibles")
                    }
                }

            } catch (e: Exception) {
                Log.e("WeatherApp", "Erreur API : ${e.message}")
                runOnUiThread {
                    onWeatherFetched("Erreur", "Inconnue", "Erreur météo")
                }
            }
        }
    }




    private fun getWeatherDescription(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "Inconnu"
            1000 -> "Ciel clair, ensoleillé"
            1100 -> "Principalement clair"
            1101 -> "Partiellement nuageux"
            1102 -> "Principalement nuageux"
            1001 -> "Nuageux"
            2000 -> "Brouillard"
            2100 -> "Brouillard léger"
            4000 -> "Bruine"
            4001 -> "Pluie"
            4200 -> "Pluie légère"
            4201 -> "Pluie forte"
            5000 -> "Neige"
            5001 -> "Neige légère"
            5100 -> "Flocons de neige"
            5101 -> "Neige abondante"
            6000 -> "Bruine glacée"
            6001 -> "Pluie glacée"
            6200 -> "Pluie glacée légère"
            6201 -> "Pluie glacée forte"
            7000 -> "Grésil"
            7101 -> "Grésil fort"
            7102 -> "Grésil léger"
            8000 -> "Orage"
            else -> "Inconnu ($weatherCode)"
        }
    }

}
