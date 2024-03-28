package com.example.opencvdemo

import android.graphics.Matrix
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import kotlin.math.sqrt

class ZoomInZoomOut : OnTouchListener {
    private lateinit var view: ImageView

    // These matrices will be used to scale points of the image
    private var matrix = Matrix()
    private var savedMatrix = Matrix()
    private var mode = NONE

    // these PointF objects are used to record the point(s) the user is touching
    private var start = PointF()
    private var mid = PointF()
    private var oldDist = 1f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        v.performClick()
        view = v as ImageView
        view.scaleType = ImageView.ScaleType.MATRIX
        val scale: Float
        dumpEvent(event)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                matrix.set(view.imageMatrix)
                savedMatrix.set(matrix)
                start[event.x] = event.y
                Log.d(TAG, "mode=DRAG")
                mode = DRAG
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                Log.d(TAG, "mode=NONE")
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                Log.d(TAG, "oldDist=$oldDist")
                if (oldDist > 5f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                    Log.d(TAG, "mode=ZOOM")
                }
            }

            MotionEvent.ACTION_MOVE -> if (mode == DRAG || mode == MOVE) {
                mode = MOVE
                Log.d(TAG, "mode=MOVE")
                matrix.set(savedMatrix)
                matrix.postTranslate(
                    event.x - start.x, event.y - start.y
                )
            } else if (mode == ZOOM) {
                val f = FloatArray(9)
                val newDist = spacing(event)
                Log.d(TAG, "newDist=$newDist")
                if (newDist > 5f) {
                    matrix.set(savedMatrix)
                    scale = newDist / oldDist
                    matrix.postScale(scale, scale, mid.x, mid.y)
                    matrix.getValues(f)
                    val scaleX = f[Matrix.MSCALE_X]
                    val scaleY = f[Matrix.MSCALE_Y]
                    Log.d(TAG, "on zoom: [$scaleX, $scaleY]")
                    if (scaleX <= MIN_ZOOM) {
                        matrix.postScale(
                            MIN_ZOOM / scaleX, MIN_ZOOM / scaleY, mid.x, mid.y
                        )
                    } else if (scaleX >= MAX_ZOOM) {
                        matrix.postScale((MAX_ZOOM) / scaleX, (MAX_ZOOM) / scaleY, mid.x, mid.y)
                    }
                }
            }
        }
        view.imageMatrix = matrix
        return true
    }

    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    /** Show an event in the LogCat view, for debugging  */
    private fun dumpEvent(event: MotionEvent) {
        val names = arrayOf(
            "DOWN",
            "UP",
            "MOVE",
            "CANCEL",
            "OUTSIDE",
            "POINTER_DOWN",
            "POINTER_UP",
            "7?",
            "8?",
            "9?"
        )
        val sb = StringBuilder()
        val action = event.action
        val actionCode = action and MotionEvent.ACTION_MASK
        sb.append("event ACTION_").append(names[actionCode])
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(action shr MotionEvent.ACTION_POINTER_ID_SHIFT)
            sb.append(")")
        }
        sb.append("[")
        for (i in 0 until event.pointerCount) {
            sb.append("#").append(i)
            sb.append("(pid ").append(event.getPointerId(i))
            sb.append(")=").append(event.getX(i).toInt())
            sb.append(",").append(event.getY(i).toInt())
            if (i + 1 < event.pointerCount) sb.append(";")
        }
        sb.append("]")
        Log.d("Touch Events ---------", sb.toString())
    }

    companion object {
        private const val TAG = "Touch"

        private const val MIN_ZOOM = 0.3f

        private const val MAX_ZOOM = 3f

        // The 3 states (events) which the user is trying to perform
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2
        const val MOVE = 3
    }
}
