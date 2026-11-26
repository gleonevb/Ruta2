package cl.rutarentable

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    private val fields = HashMap<String, EditText>()

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }
        fun label(t: String, size: Float = 14f) = TextView(this).apply { text = t; textSize = size }.also { col.addView(it) }

        label("Ruta Rentable", 24f)
        label("Define tus mínimos. Pon 0 en un límite para ignorarlo.\n")

        val labels = mapOf(
            "minPerHour" to "Ganancia mínima por hora ($)",
            "minPerKm" to "Precio mínimo por km ($)",
            "maxPickupKm" to "Máx. distancia pick up (km)",
            "maxPickupMin" to "Máx. tiempo pick up (min)",
            "maxTripKm" to "Máx. distancia viaje / drop (km)",
            "maxTripMin" to "Máx. tiempo viaje / drop (min)"
        )
        for (k in Prefs.keys) {
            label(labels[k]!!)
            val et = EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                val v = Prefs.get(this@MainActivity, k)
                setText(if (v == v.toLong().toFloat()) v.toLong().toString() else v.toString())
            }
            fields[k] = et
            col.addView(et, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        col.addView(Button(this).apply {
            text = "Guardar"
            setOnClickListener {
                for ((k, et) in fields) Prefs.set(this@MainActivity, k, et.text.toString().replace(",", ".").toFloatOrNull() ?: 0f)
                Toast.makeText(this@MainActivity, "Guardado", Toast.LENGTH_SHORT).show()
            }
        })
        col.addView(Button(this).apply {
            text = "Activar servicio (Accesibilidad)"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        label("\nPasos: 1) Guarda tus valores. 2) Activa 'Ruta Rentable' en Accesibilidad. 3) Abre Uber Driver. " +
            "Si Android lo bloquea: Ajustes > Apps > Ruta Rentable > ⋮ > 'Permitir ajustes restringidos'.")

        setContentView(ScrollView(this).apply { addView(col) })
    }
}
