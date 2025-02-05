import retrofit2.http.GET
import retrofit2.http.Query

// Définition de l'interface Retrofit pour interagir avec l'API Google Geocoding
interface GeocodingApi {
    @GET("maps/api/geocode/json")
    suspend fun getAddressFromCoordinates(
        @Query("latlng") latlng: String, // Les coordonnées (latitude, longitude)
        @Query("key") apiKey: String // Votre clé API Google
    ): GeocodingResponse
}
