package com.anwesh.uiprojects.linkedordertrackerview

/**
 * Created by anweshmishra on 07/06/18.
 */

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.graphics.*

val LOT_NODES : Int = 5

class LinkedOrderTrackerView(ctx : Context) : View (ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var prevScale : Float = 0f, var dir : Float = 0f, var j : Int = 0) {

        val scales : Array<Float> = arrayOf(0f, 0f)

        fun update(stopcb : (Float) -> Unit) {
            scales[j] += 0.1f * dir
            if (Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += dir.toInt()
                if (j == scales.size || j == -1) {
                    scales[j] = prevScale + dir
                    dir = 0f
                    prevScale = scales[j]
                    stopcb(prevScale)
                }
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch (ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LOTNode (var i : Int, val state : State = State()) {

        private var next : LOTNode? = null

        private var prev : LOTNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < LOT_NODES - 1) {
                next = LOTNode(i +1)
                next?.prev = this
            }
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

        fun draw(canvas : Canvas, paint : Paint) {
            prev?.draw(canvas, paint)
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            paint.strokeWidth = Math.min(w, h) / 50
            paint.strokeCap = Paint.Cap.ROUND
            val wGap : Float = w / LOT_NODES
            canvas.save()
            canvas.translate(wGap * i, h/2)
            canvas.drawCircle(wGap, 0f, wGap/5 * state.scales[1], paint)
            canvas.drawLine(0f, 0f, wGap * state.scales[0], 0f, paint)
            canvas.restore()
        }

        fun getNext(dir : Int, cb : () -> Unit) : LOTNode {
            var curr : LOTNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedOrderTrack (var i : Int) {

        var curr : LOTNode = LOTNode(0)

        var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            paint.color = Color.parseColor("#512DA8")
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val r : Float = Math.min(w, h)/(LOT_NODES * 5)
            canvas.drawCircle(r, h/2, r, paint)
            curr.draw(canvas, paint)
        }

        fun update(stopcb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                stopcb(it)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            curr.startUpdating(startcb)
        }
    }

    data class Renderer(var view : LinkedOrderTrackerView) {

        private val animator : Animator = Animator(view)

        private val lot : LinkedOrderTrack = LinkedOrderTrack(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            lot.draw(canvas, paint)
            animator.animate {
                lot.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lot.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity): LinkedOrderTrackerView {
            val view : LinkedOrderTrackerView = LinkedOrderTrackerView(activity)
            activity.setContentView(view)
            return view
        }
    }
}