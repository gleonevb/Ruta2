package cl.rutarentable

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import java.util.Locale

class OfferService : AccessibilityService() {
    private var wm: WindowManager? = null
    private var overlay: TextView? = null
    private val handler = Handler(Looper.getMainLooper())
    private val hide = Runnable { removeOverlay() }

    override fun onServiceConnected() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName?.toString() != "com.ubercab.driver") return
        val root = rootInActiveWindow ?: return
        val texts = ArrayList<String>()
        collect(root, texts, 0)
        val offer = Parser.parse(texts) ?: return
        val v = Parser.evaluate(offer, Prefs.load(this))
        show(offer, v)
        handler.removeCallbacks(hide)
        handler.postDelayed(hide, 6000)
    }

    private fun collect(n: AccessibilityNodeInfo?, out: MutableList<String>, depth: Int) {
        if (n == null || depth > 40) return
        n.text?.toString()?.let { if (it.isNotBlank()) out.add(it) }
        n.contentDescription?.toString()?.let { if (it.isNotBlank()) out.add(it) }
        for (i in 0 until n.childCount) collect(n.getChild(i), out, depth + 1)
    }

    private fun fmt(d: Double) = String.format(Locale("es", "CL"), "%,.0f", d)

    private fun show(o: Offer, v: Verdict) {
        val title = if (v.accept) "✅ ACEPTAR" else "❌ RECHAZAR"
        val extra = if (v.reasons.isEmpty()) "" else "\n" + v.reasons.joinToString(" · ")
        val body = "$title\n$${fmt(v.perKm)}/km · $${fmt(v.perHour)}/h\n" +
            String.format(Locale.US, "Total %.1f km · %.0f min", v.totalKm, v.totalMin) +
            String.format(Locale.US, "\nPickup %.1f km %.0f min · Viaje %.1f km %.0f min",
                o.pickupKm, o.pickupMin, o.tripKm, o.tripMin) + extra
        val tv = overlay ?: TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(24, 48, 24, 24)
            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            lp.gravity = Gravity.TOP
            wm?.addView(this, lp)
            this@OfferService.overlay = this
        }
        tv.setBackgroundColor(if (v.accept) Color.parseColor("#E6138A36") else Color.parseColor("#E6C62828"))
        tv.text = body
    }

    private fun removeOverlay() {
        overlay?.let { try { wm?.removeView(it) } catch (e: Exception) {} }
        overlay = null
    }

    override fun onInterrupt() { removeOverlay() }
    override fun onDestroy() { removeOverlay(); super.onDestroy() }
}
