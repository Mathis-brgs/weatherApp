// Cette classe contient la réponse de l'API Geocoding
data class GeocodingResponse(
    val results: List<GeocodingResult> // Liste des résultats de géocodification
)

// Cette classe représente chaque résultat de géocodification
data class GeocodingResult(
    val formatted_address: String, // L'adresse complète
    val address_components: List<AddressComponent> // Liste des composants d'adresse
)

// Cette classe représente un composant d'adresse (par exemple, ville, pays, etc.)
data class AddressComponent(
    val long_name: String, // Le nom long du composant (ex. "Paris")
    val types: List<String> // Types d'adresse (par exemple, "locality", "administrative_area_level_1", etc.)
)
