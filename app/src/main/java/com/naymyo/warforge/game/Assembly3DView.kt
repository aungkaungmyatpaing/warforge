package com.naymyo.warforge.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import com.naymyo.warforge.gl.ViewMode
import com.naymyo.warforge.poly.Palette
import com.naymyo.warforge.poly.shade
import com.naymyo.warforge.ui.InspectSurfaceView
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/** What the current touch is doing. Inner classes may not declare enums of their own. */
private enum class Mode { IDLE, PENDING, SCROLL, DRAG, ORBIT }

/**
 * The assembly board.
 *
 * A 3D viewport with a parts tray drawn over it. Empty slots show as translucent blue
 * shapes inside the vehicle; drag a part up out of the tray and drop it on the right one.
 * Drag on the model itself and the camera turns instead, which is what makes fitting an
 * engine into a hull possible at all - you have to look inside to see where it goes.
 */
class Assembly3DView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    /** Fired when a part or module is fitted. */
    var onPlaced: ((LevelState.Item) -> Unit)? = null

    /** Fired on a rejected drop - wrong slot, or a decoy. */
    var onMistake: (() -> Unit)? = null

    /** Fired once the last piece is in. */
    var onComplete: (() -> Unit)? = null

    /** Fired when the player taps a tray item. */
    var onSelected: ((LevelState.Item) -> Unit)? = null

    /** Fired when the build moves on to the next stage. */
    var onStageChanged: ((Stage?) -> Unit)? = null

    /** Assist mode rings the correct slot as soon as a part leaves the tray. */
    var showGhosts: Boolean = true

    val viewport = InspectSurfaceView(context).apply {
        interactive = false
        autoSpin = false
    }

    private val overlay = Overlay(context)
    private var level: LevelState? = null
    private var palette = Palette.of(0x7A6A45, 0x2B261D, 0x8E897A, 0x38474E, 0xA8402F)

    init {
        addView(viewport, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(overlay, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun setLevel(state: LevelState) {
        level = state
        palette = state.vehicle.palette
        overlay.reset()
        // The scene builds on a worker thread; push the build state once it exists.
        viewport.show(state.vehicle) { syncScene(announceStage = true) }
    }

    fun flashHint(item: LevelState.Item) {
        overlay.hint(item)
    }

    fun mode(mode: ViewMode) {
        viewport.mode = mode
    }

    /**
     * Pushes what is fitted and what is still open to the renderer, and picks the view
     * that suits the stage: see-through while the guts are going in, solid once the
     * armour is on.
     */
    private fun syncScene(announceStage: Boolean) {
        val state = level ?: return
        val ghosts = state.openSlots().map { it.id }.toSet()
        viewport.setBuildState(state.placedSlots(), ghosts)
        viewport.mode = when (state.stage) {
            Stage.POWERPACK, Stage.FIGHTING, Stage.ARMOUR -> ViewMode.XRAY
            else -> ViewMode.SOLID
        }
        if (announceStage) onStageChanged?.invoke(state.stage)
        viewport.requestRender()
        overlay.invalidate()
    }

    // ======================================================================
    // Overlay: tray, drag, and the aiming ring
    // ======================================================================

    private inner class Overlay(context: Context) : View(context) {

        private val tray = RectF()
        private var rows = 1
        private var cellW = 0f
        private var cellH = 0f
        private var unitScale = 1f
        private var scroll = 0f
        private var maxScroll = 0f

        private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        private val panel = Paint(Paint.ANTI_ALIAS_FLAG)
        private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
        private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        private val pathCache = HashMap<LevelState.Slot, List<Pair<Path, Int>>>()
        private val projected = FloatArray(2)

        private var mode = Mode.IDLE
        private var downX = 0f
        private var downY = 0f
        private var lastX = 0f
        private var lastY = 0f
        private var lastPinch = 0f
        private var candidate: LevelState.Item? = null
        private var dragging: LevelState.Item? = null
        private var dragX = 0f
        private var dragY = 0f
        private var aimSlot: LevelState.Slot? = null
        private var aimDistance = Float.MAX_VALUE

        /** How far the finger is from the slot the dragged piece actually belongs in. */
        private var ownDistance = Float.MAX_VALUE
        /**
         * The piece a tap has armed. Dragging is the natural gesture, but a small slot
         * seen edge-on is a hard target to drag onto, so tapping the piece and then
         * tapping where it goes does the same job with a steadier hand.
         */
        private var armed: LevelState.Item? = null
        private var shakeStart = 0L
        private var hintUntil = 0L
        private var hintSlot: LevelState.Slot? = null
        private val slop = ViewConfiguration.get(context).scaledTouchSlop

        fun reset() {
            pathCache.clear()
            scroll = 0f
            mode = Mode.IDLE
            dragging = null
            candidate = null
            aimSlot = null
            hintSlot = null
            invalidate()
        }

        fun hint(item: LevelState.Item) {
            hintSlot = item.slot
            hintUntil = SystemClock.uptimeMillis() + HINT_MS
            viewport.selectedId = item.slot.id
            invalidate()
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            layoutTray()
        }

        private fun layoutTray() {
            val h = height.toFloat()
            val trayH = (h * TRAY_FRACTION).coerceIn(dp(104f), dp(220f))
            tray.set(0f, h - trayH, width.toFloat(), h)

            val count = (level?.tray?.size ?: 0).coerceAtLeast(1)
            val gap = dp(8f)
            val availW = tray.width() - dp(24f)
            val availH = tray.height() - dp(18f)
            var r = 1
            while (r < MAX_ROWS && columnsFor(r, availW, availH, gap) * r < count) r++
            rows = r
            cellH = min(dp(96f), (availH - gap * (r - 1)) / r)
            cellW = cellH * 1.15f
            val cols = ceil(count / r.toFloat()).toInt().coerceAtLeast(1)
            maxScroll = (dp(24f) + cols * cellW + (cols - 1) * gap - tray.width())
                .coerceAtLeast(0f)
            scroll = scroll.coerceIn(0f, maxScroll)

            val biggest = level?.tray.orEmpty()
                .maxOfOrNull { maxOf(it.slot.width, it.slot.height) } ?: 1f
            unitScale = min(cellW * 0.78f, cellH * 0.56f) / biggest.coerceAtLeast(1f)
            label.textSize = dp(10.5f)
            ring.strokeWidth = dp(2.5f)
        }

        private fun columnsFor(r: Int, availW: Float, availH: Float, gap: Float): Int {
            val h = min(dp(96f), (availH - gap * (r - 1)) / r)
            return ((availW + gap) / (h * 1.15f + gap)).toInt().coerceAtLeast(1)
        }

        // ------------------------------------------------------------------

        override fun onDraw(canvas: Canvas) {
            val state = level ?: return
            var animating = false
            val now = SystemClock.uptimeMillis()

            // Aiming ring over the slot the dragged part is closest to.
            dragging?.let { item ->
                aimSlot?.let { slot ->
                    if (viewport.project(slot.wx, slot.wy, slot.wz, projected)) {
                        // Amber once the drop would land: assist mode says so as soon as
                        // the finger is in range, otherwise the ring is just a pointer.
                        val willFit = ownDistance <= snapPx()
                        ring.color = if (showGhosts && willFit) ACCENT else ACCENT_DIM
                        canvas.drawCircle(projected[0], projected[1], snapPx(), ring)
                        animating = true
                    }
                }
            }

            if (hintSlot != null && now < hintUntil) {
                hintSlot?.let { slot ->
                    if (viewport.project(slot.wx, slot.wy, slot.wz, projected)) {
                        val pulse = sin((hintUntil - now) / 90f) * 0.5f + 0.5f
                        ring.color = ACCENT
                        ring.alpha = (90 + 165 * pulse).toInt()
                        canvas.drawCircle(projected[0], projected[1], snapPx() * 1.1f, ring)
                        ring.alpha = 255
                    }
                }
                animating = true
            } else if (hintSlot != null) {
                hintSlot = null
                viewport.selectedId = null
            }

            drawStageBanner(canvas, state)

            // --- tray -----------------------------------------------------
            panel.color = TRAY_BG
            canvas.drawRect(tray, panel)
            panel.color = TRAY_EDGE
            canvas.drawRect(tray.left, tray.top, tray.right, tray.top + dp(1.5f), panel)

            canvas.save()
            canvas.clipRect(tray)
            val shakeAge = now - shakeStart
            val shake = if (shakeAge < SHAKE_MS) {
                animating = true
                sin(shakeAge / 22f) * dp(7f) * (1f - shakeAge / SHAKE_MS.toFloat())
            } else 0f
            for ((i, item) in state.tray.withIndex()) {
                if (item === dragging) continue
                val cx = cellCenterX(i)
                if (cx + cellW < tray.left || cx - cellW > tray.right) continue
                val wobble = if (item === candidate && shake != 0f) shake else 0f
                drawCell(canvas, item, cx + wobble, cellCenterY(i))
            }
            canvas.restore()
            if (maxScroll > 0f) drawScrollbar(canvas)

            // --- the piece in hand ---------------------------------------
            dragging?.let { item ->
                val s = unitScale * 1.6f
                canvas.save()
                canvas.translate(dragX, dragY)
                canvas.scale(s, s)
                canvas.translate(-item.slot.bx, -item.slot.by)
                drawSlot(canvas, item.slot, 0.92f)
                canvas.restore()
            }

            if (animating) postInvalidateOnAnimation()
        }

        private fun drawStageBanner(canvas: Canvas, state: LevelState) {
            val stage = state.stage ?: return
            label.textAlign = Paint.Align.LEFT
            label.color = ACCENT
            canvas.drawText(
                "STAGE ${state.stageIndex + 1}/${state.stages.size}  ·  ${stage.label.uppercase()}",
                dp(14f), dp(20f), label,
            )
            label.color = INK_DIM
            canvas.drawText(stage.hint, dp(14f), dp(36f), label)
            label.textAlign = Paint.Align.CENTER
        }

        private fun drawCell(canvas: Canvas, item: LevelState.Item, cx: Float, cy: Float) {
            val halfW = cellW * 0.5f
            val halfH = cellH * 0.5f
            panel.color = when {
                item === armed -> CELL_ARMED
                item.slot.internal -> CELL_INTERNAL
                else -> CELL_BG
            }
            canvas.drawRoundRect(
                cx - halfW, cy - halfH, cx + halfW, cy + halfH, dp(10f), dp(10f), panel,
            )
            val fit = min(
                cellW * 0.78f / item.slot.width.coerceAtLeast(1f),
                cellH * 0.54f / item.slot.height.coerceAtLeast(1f),
            )
            val s = unitScale.coerceIn(fit * 0.34f, fit)
            canvas.save()
            canvas.translate(cx, cy - dp(6f))
            canvas.scale(s, s)
            canvas.translate(-item.slot.bx, -item.slot.by)
            drawSlot(canvas, item.slot, 1f)
            canvas.restore()

            label.color = INK_DIM
            canvas.drawText(
                ellipsize(item.slot.name, cellW - dp(10f)), cx, cy + halfH - dp(7f), label,
            )
        }

        private fun drawSlot(canvas: Canvas, slot: LevelState.Slot, alpha: Float) {
            for ((path, color) in prepare(slot)) {
                fill.color = color
                fill.alpha = (255 * alpha).toInt()
                canvas.drawPath(path, fill)
            }
            fill.alpha = 255
        }

        /**
         * Tray art. Parts wear the vehicle's livery; modules keep their own colour code,
         * so an engine looks the same orange in the tray as it does inside the hull.
         */
        private fun prepare(slot: LevelState.Slot): List<Pair<Path, Int>> =
            pathCache.getOrPut(slot) {
                val pal = level?.vehicle?.modules
                    ?.firstOrNull { "mod_" + it.id == slot.id }?.kind?.palette ?: palette
                slot.polys.map { poly ->
                    val path = Path()
                    path.moveTo(poly.pts[0], poly.pts[1])
                    var i = 2
                    while (i + 1 < poly.pts.size) {
                        path.lineTo(poly.pts[i], poly.pts[i + 1]); i += 2
                    }
                    path.close()
                    path to shade(pal.colorFor(poly.role), poly.tone)
                }
            }

        private fun drawScrollbar(canvas: Canvas) {
            val trackW = tray.width() * 0.4f
            val left = tray.left + (tray.width() - trackW) * 0.5f
            val y = tray.bottom - dp(6f)
            panel.color = TRAY_EDGE
            canvas.drawRect(left, y, left + trackW, y + dp(3f), panel)
            panel.color = ACCENT
            val frac = if (maxScroll > 0f) scroll / maxScroll else 0f
            val thumb = trackW * 0.34f
            canvas.drawRect(
                left + (trackW - thumb) * frac, y,
                left + (trackW - thumb) * frac + thumb, y + dp(3f), panel,
            )
        }

        private fun ellipsize(text: String, maxWidth: Float): String {
            if (label.measureText(text) <= maxWidth) return text
            var end = text.length
            while (end > 1 && label.measureText(text.substring(0, end) + "…") > maxWidth) end--
            return text.substring(0, end) + "…"
        }

        // ------------------------------------------------------------------

        private fun cellCenterX(i: Int) =
            tray.left + dp(12f) + cellW * 0.5f + (i / rows) * (cellW + dp(8f)) - scroll

        private fun cellCenterY(i: Int) =
            tray.top + dp(9f) + cellH * 0.5f + (i % rows) * (cellH + dp(8f))

        private fun itemAt(x: Float, y: Float): LevelState.Item? {
            val state = level ?: return null
            for ((i, item) in state.tray.withIndex()) {
                if (abs(x - cellCenterX(i)) <= cellW * 0.5f &&
                    abs(y - cellCenterY(i)) <= cellH * 0.5f
                ) return item
            }
            return null
        }

        private fun snapPx() = dp(66f)

        /**
         * Works out what the finger is over: the nearest open slot, for the ring, and
         * separately how close it is to the one the dragged piece belongs in.
         *
         * The two are deliberately not the same test. Internals sit right on top of one
         * another - a radiator alongside the engine it cools, a fuel cell under the
         * ammunition - and project to nearly the same point, so "nearest slot wins"
         * would turn fitting an engine into a pixel contest with its own cooling system.
         * What the game asks is whether the player knows *where* the engine goes.
         */
        private fun updateAim(x: Float, y: Float) {
            val state = level ?: return
            val dragged = dragging ?: armed
            var best: LevelState.Slot? = null
            var bestD = Float.MAX_VALUE
            var own = Float.MAX_VALUE
            for (slot in state.openSlots()) {
                if (!viewport.project(slot.wx, slot.wy, slot.wz, projected)) continue
                val d = hypot(x - projected[0], y - projected[1])
                if (d < bestD) { bestD = d; best = slot }
                if (dragged != null && slot.id == dragged.slot.id) own = d
            }
            aimSlot = best
            aimDistance = bestD
            ownDistance = own
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val state = level ?: return false
            viewport.noteTouch()
            if (state.isComplete) return false

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x; downY = event.y
                    lastX = event.x; lastY = event.y
                    if (tray.contains(event.x, event.y)) {
                        candidate = itemAt(event.x, event.y)
                        mode = if (candidate != null) Mode.PENDING else Mode.SCROLL
                    } else {
                        mode = Mode.ORBIT
                    }
                    parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (mode == Mode.ORBIT && event.pointerCount >= 2) {
                        lastPinch = spread(event)
                    }
                    return true
                }

                // Re-anchor on the finger that is left, or the camera lurches.
                MotionEvent.ACTION_POINTER_UP -> {
                    val remaining = if (event.actionIndex == 0) 1 else 0
                    lastX = event.getX(remaining)
                    lastY = event.getY(remaining)
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    when (mode) {
                        Mode.PENDING ->
                            if (hypot(event.x - downX, event.y - downY) > slop) {
                                // Up and out of the tray picks the part up; sideways scrolls.
                                mode = if (abs(event.y - downY) > abs(event.x - downX)) {
                                    dragging = candidate
                                    candidate?.let { onSelected?.invoke(it) }
                                    Mode.DRAG
                                } else {
                                    Mode.SCROLL
                                }
                            }

                        Mode.SCROLL -> scroll = (scroll - dx).coerceIn(0f, maxScroll)

                        Mode.ORBIT ->
                            if (event.pointerCount >= 2) {
                                val now = spread(event)
                                if (lastPinch > 1f && now > 1f) viewport.zoomBy(now / lastPinch)
                                lastPinch = now
                            } else {
                                viewport.orbitBy(dx, dy)
                            }

                        else -> Unit
                    }
                    if (mode == Mode.DRAG) {
                        dragX = event.x; dragY = event.y
                        updateAim(event.x, event.y)
                    }
                    lastX = event.x; lastY = event.y
                    invalidate()
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    when (mode) {
                        Mode.DRAG -> dragging?.let { drop(state, it, event.x, event.y) }

                        // A tap in the tray arms a piece, or puts it back down.
                        Mode.PENDING -> candidate?.let {
                            armed = if (armed === it) null else it
                            armed?.let { a -> onSelected?.invoke(a) }
                        }

                        // A tap on the model fits whatever is armed.
                        Mode.ORBIT -> if (
                            abs(event.x - downX) < slop && abs(event.y - downY) < slop
                        ) {
                            armed?.let { drop(state, it, event.x, event.y) }
                        }

                        else -> Unit
                    }
                    mode = Mode.IDLE
                    dragging = null
                    candidate = null
                    aimSlot = null
                    ownDistance = Float.MAX_VALUE
                    invalidate()
                    return true
                }

                MotionEvent.ACTION_CANCEL -> {
                    mode = Mode.IDLE; dragging = null; candidate = null; aimSlot = null
                    invalidate()
                    return true
                }
            }
            return super.onTouchEvent(event)
        }

        private fun spread(event: MotionEvent) =
            hypot(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))

        private fun drop(state: LevelState, item: LevelState.Item, x: Float, y: Float) {
            // Dropping back in the tray is a change of mind, not a mistake.
            if (tray.contains(x, y)) { android.util.Log.i("WFTOUCH", "drop in tray"); return }

            updateAim(x, y)
            val target = if (ownDistance <= snapPx()) item.slot.id else aimSlot?.id
            val stageBefore = state.stageIndex
            if (state.tryPlace(item, target)) {
                armed = null
                onPlaced?.invoke(item)
                layoutTray()
                syncScene(announceStage = state.stageIndex != stageBefore)
                if (state.isComplete) onComplete?.invoke()
            } else {
                candidate = item
                shakeStart = SystemClock.uptimeMillis()
                onMistake?.invoke()
            }
            invalidate()
        }

        private fun dp(v: Float) = v * resources.displayMetrics.density
    }

    private companion object {
        const val TRAY_FRACTION = 0.28f
        const val MAX_ROWS = 3
        const val SHAKE_MS = 340
        const val HINT_MS = 2600L

        val ACCENT = Color.parseColor("#E8A53A")
        val ACCENT_DIM = Color.parseColor("#6E5A2E")
        val INK_DIM = Color.parseColor("#7B92A5")
        val TRAY_BG = Color.parseColor("#141C25")
        val TRAY_EDGE = Color.parseColor("#24323F")
        val CELL_BG = Color.parseColor("#1C2733")
        val CELL_INTERNAL = Color.parseColor("#23303D")
        val CELL_ARMED = Color.parseColor("#3A4E30")
    }
}
