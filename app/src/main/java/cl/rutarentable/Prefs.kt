package cl.rutarentable

import android.content.Context

data class Config(
    val minPerHour: Double,
    val minPerKm: Double,
    val maxPickupKm: Double,
    val maxPickupMin: Double,
    val maxTripKm: Double,
    val maxTripMin: Double
)

object Prefs {
    private const val FILE = "cfg"
    val keys = listOf("minPerHour", "minPerKm", "maxPickupKm", "maxPickupMin", "maxTripKm", "maxTripMin")
    private val defaults = mapOf(
        "minPerHour" to 10000f, "minPerKm" to 400f,
        "maxPickupKm" to 3f, "maxPickupMin" to 8f,
        "maxTripKm" to 0f, "maxTripMin" to 0f
    )

    fun get(c: Context, key: String): Float =
        c.getSharedPreferences(FILE, Context.MODE_PRIVATE).getFloat(key, defaults[key] ?: 0f)

    fun set(c: Context, key: String, v: Float) {
        c.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putFloat(key, v).apply()
    }

    fun load(c: Context) = Config(
        get(c, "minPerHour").toDouble(), get(c, "minPerKm").toDouble(),
        get(c, "maxPickupKm").toDouble(), get(c, "maxPickupMin").toDouble(),
        get(c, "maxTripKm").toDouble(), get(c, "maxTripMin").toDouble()
    )
}
