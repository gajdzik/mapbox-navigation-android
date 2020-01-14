package com.mapbox.navigation.base.trip.model

import com.mapbox.api.directions.v5.models.LegStep
import com.mapbox.geojson.Point

class RouteStepProgressNavigation private constructor(
    private val stepIndex: Int = 0,
    private val step: LegStep? = null,
    private val stepPoints: List<Point>? = null,
    private val distanceRemaining: Double = 0.0,
    private val distanceTraveled: Double = 0.0,
    private val fractionTraveled: Float = 0f,
    private val durationRemaining: Double = 0.0,
    private val builder: Builder
) {

    /**
     * Index representing the current step the user is on.
     *
     * @return an integer representing the current step the user is on
     */
    fun stepIndex(): Int = stepIndex

    /**
     * Returns the current step the user is traversing along.
     *
     * @return a [LegStep] representing the step the user is currently on
     */
    fun step(): LegStep? = step

    /**
     * Provides a list of points that represent the current step
     * step geometry.
     *
     * @return list of points representing the current step
     */
    fun stepPoints() = stepPoints

    /**
     * Total distance in meters from user to end of step.
     *
     * @return double value representing the distance the user has remaining till they reach the end
     * of the current step. Uses unit meters.
     */
    fun distanceRemaining(): Double = distanceRemaining

    /**
     * Returns distance user has traveled along current step in unit meters.
     *
     * @return double value representing the distance the user has traveled so far along the current
     * step. Uses unit meters.
     */
    fun distanceTraveled(): Double = distanceTraveled

    /**
     * Get the fraction traveled along the current step, this is a float value between 0 and 1 and
     * isn't guaranteed to reach 1 before the user reaches the next step (if another step exist in route).
     *
     * @return a float value between 0 and 1 representing the fraction the user has traveled along
     * the current step.
     */
    fun fractionTraveled(): Float = fractionTraveled

    /**
     * Provides the duration remaining in seconds till the user reaches the end of the current step.
     *
     * @return `long` value representing the duration remaining till end of step, in unit seconds.
     */
    fun durationRemaining(): Double = durationRemaining

    fun toBuilder() = builder

    data class Builder(
        private var stepIndex: Int = 0,
        private var step: LegStep? = null,
        private var stepPoints: List<Point>? = null,
        private var distanceRemaining: Double = 0.0,
        private var distanceTraveled: Double = 0.0,
        private var fractionTraveled: Float = 0f,
        private var durationRemaining: Double = 0.0
    ) {

        fun stepIndex(stepIndex: Int) =
            apply { this.stepIndex = stepIndex }

        fun step(step: LegStep) = apply { this.step = step }

        fun stepPoints(stepPoints: List<Point>) =
            apply { this.stepPoints = stepPoints }

        fun distanceRemaining(distanceRemaining: Double) =
            apply { this.distanceRemaining = distanceRemaining }

        fun distanceTraveled(distanceTraveled: Double) =
            apply { this.distanceTraveled = distanceTraveled }

        fun fractionTraveled(fractionTraveled: Float) =
            apply { this.fractionTraveled = fractionTraveled }

        fun durationRemaining(durationRemaining: Double) =
            apply { this.durationRemaining = durationRemaining }

        fun build(): RouteStepProgressNavigation {
            distanceTraveled = calculateDistanceTraveled(legStep, distanceRemaining)
            fractionTraveled = calculateFractionTraveled(legStep, distanceTraveled)
            durationRemaining = calculateDurationRemaining(legStep, fractionTraveled)

            validate()

            return RouteStepProgressNavigation(
                stepIndex,
                step,
                stepPoints,
                distanceRemaining,
                distanceTraveled,
                fractionTraveled,
                durationRemaining,
                this
            )
        }

        private fun calculateDistanceTraveled(step: LegStep, distanceRemaining: Double): Double {
            val distanceTraveled = step.distance() - distanceRemaining
            return when {
                distanceTraveled < 0 -> 0.0
                else -> distanceTraveled
            }
        }

        private fun calculateFractionTraveled(step: LegStep, distanceTraveled: Double): Float {
            val currentDistance = step.distance()
            when {
                currentDistance <= 0 -> return 1f
                else -> {
                    val fractionTraveled = (distanceTraveled / currentDistance).toFloat()
                    return when {
                        fractionTraveled < 0 -> return 0f
                        else -> fractionTraveled
                    }
                }
            }
        }

        private fun calculateDurationRemaining(
            step: LegStep,
            fractionTraveled: Float
        ): Double = (1 - fractionTraveled) * step.duration()
    }
}
