package cl.rutarentable

data class Offer(
    val price: Double,
    val pickupMin: Double, val pickupKm: Double,
    val tripMin: Double, val tripKm: Double
)

data class Verdict(val accept: Boolean, val perKm: Double, val perHour: Double,
                   val totalKm: Double, val totalMin: Double, val reasons: List<String>)

object Parser {
    private val money = Regex("""(?:CLP|CL\$|\$)\s?(\d[\d.,]*)""", RegexOption.IGNORE_CASE)
    private val pair = Regex(
        """(?:(\d+)\s*h\w*\s*)?(\d+)\s*min\w*\s*[(·•\-]?\s*(\d+(?:[.,]\d+)?)\s*km""",
        RegexOption.IGNORE_CASE
    )
    private val button = Regex("""aceptar|accept|seleccionar|select|match""", RegexOption.IGNORE_CASE)

    fun parseMoney(s: String): Double {
        val t = s.trim().trimEnd('.', ',')
        return if (Regex("""^\d{1,3}([.,]\d{3})+$""").matches(t)) t.replace(".", "").replace(",", "").toDouble()
        else t.replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    fun parse(texts: List<String>): Offer? {
        val all = texts.joinToString(" ")
        if (!button.containsMatchIn(all)) return null
        val price = money.find(all)?.let { parseMoney(it.groupValues[1]) } ?: return null
        if (price <= 0) return null
        val pairs = pair.findAll(all).toList()
        if (pairs.size < 2) return null
        fun mins(m: MatchResult): Double =
            (m.groupValues[1].toDoubleOrNull() ?: 0.0) * 60 + (m.groupValues[2].toDoubleOrNull() ?: 0.0)
        fun km(m: MatchResult): Double = m.groupValues[3].replace(",", ".").toDoubleOrNull() ?: 0.0
        return Offer(price, mins(pairs[0]), km(pairs[0]), mins(pairs[1]), km(pairs[1]))
    }

    fun evaluate(o: Offer, c: Config): Verdict {
        val totalKm = (o.pickupKm + o.tripKm).coerceAtLeast(0.1)
        val totalMin = (o.pickupMin + o.tripMin).coerceAtLeast(1.0)
        val perKm = o.price / totalKm
        val perHour = o.price / (totalMin / 60.0)
        val why = ArrayList<String>()
        if (perHour < c.minPerHour) why.add("$/h bajo")
        if (perKm < c.minPerKm) why.add("$/km bajo")
        if (c.maxPickupKm > 0 && o.pickupKm > c.maxPickupKm) why.add("pick up lejos")
        if (c.maxPickupMin > 0 && o.pickupMin > c.maxPickupMin) why.add("pick up lento")
        if (c.maxTripKm > 0 && o.tripKm > c.maxTripKm) why.add("viaje largo")
        if (c.maxTripMin > 0 && o.tripMin > c.maxTripMin) why.add("viaje lento")
        return Verdict(why.isEmpty(), perKm, perHour, totalKm, totalMin, why)
    }
}
