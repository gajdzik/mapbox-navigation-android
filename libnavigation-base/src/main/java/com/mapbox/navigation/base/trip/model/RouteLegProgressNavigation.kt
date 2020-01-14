package com.mapbox.navigation.base.trip.model

import com.mapbox.api.directions.v5.models.LegStep
import com.mapbox.api.directions.v5.models.RouteLeg
import com.mapbox.navigation.base.extensions.ifNonNull

class RouteLegProgressNavigation private constructor(
    private val legIndex: Int = 0,
    private val routeLeg: RouteLeg? = null,
    private val distanceTraveled: Double = 0.0,
    private val distanceRemaining: Double = 0.0,
    private val durationRemaining: Double = 0.0,
    private val fractionTraveled: Float = 0f,
    private val currentStepProgress: RouteStepProgressNavigation? = null,
    private val upComingStep: LegStep? = null,
    private val builder: Builder
) {

    /**
     * Index representing the current leg the user is on. If the directions route currently in use
     * contains more then two waypoints, the route is likely to have multiple legs representing the
     * distance between the two points.
     *
     * @return an integer representing the current leg the user is on
     */
    fun legIndex(): Int = legIndex

    /**
     * This [RouteLeg] geometry.
     *
     * @return route leg geometry
     */
    fun routeLeg(): RouteLeg? = routeLeg

    /**
     * Total distance traveled in meters along current leg.
     *
     * @return a double value representing the total distance the user has traveled along the current
     * leg, using unit meters.
     */
    fun distanceTraveled(): Double = distanceTraveled

    /**
     * Provides the duration remaining in seconds till the user reaches the end of the route.
     *
     * @return long value representing the duration remaining till end of route, in unit seconds
     */
    fun distanceRemaining(): Double = distanceRemaining

    /**
     * Provides the duration remaining in seconds till the user reaches the end of the current step.
     *
     * @return long value representing the duration remaining till end of step, in unit seconds.
     */
    fun durationRemaining(): Double = durationRemaining

    /**
     * Get the fraction traveled along the current leg, this is a float value between 0 and 1 and
     * isn't guaranteed to reach 1 before the user reaches the next waypoint.
     *
     * @return a float value between 0 and 1 representing the fraction the user has traveled along the
     * current leg
     */
    fun fractionTraveled(): Float = fractionTraveled

    /**
     * Gives a [RouteStepProgressNavigation] object with information about the particular step the user
     * is currently on.
     *
     * @return a [RouteStepProgressNavigation] object
     */
    fun currentStepProgress(): RouteStepProgressNavigation? = currentStepProgress

    /**
     * Get the next/upcoming step immediately after the current step. If the user is on the last step
     * on the last leg, this will return null since a next step doesn't exist.
     *
     * @return a [LegStep] representing the next step the user will be on.
     */
    fun upComingStep(): LegStep? = upComingStep

    fun toBuilder() = builder

    data class Builder(
        private var legIndex: Int = 0,
        private var routeLeg: RouteLeg? = null,
        private var distanceTraveled: Double = 0.0,
        private var distanceRemaining: Double = 0.0,
        private var durationRemaining: Double = 0.0,
        private var fractionTraveled: Float = 0f,
        private var currentStepProgress: RouteStepProgressNavigation? = null,
        private var upComingStep: LegStep? = null
    ) {

        fun legIndex(legIndex: Int) = apply { this.legIndex = legIndex }

        fun routeLeg(routeLeg: RouteLeg) = apply { this.routeLeg = routeLeg }

        fun distanceTraveled(distanceTraveled: Double) =
            apply { this.distanceTraveled = distanceTraveled }

        fun distanceRemaining(distanceRemaining: Double) =
            apply { this.distanceRemaining = distanceRemaining }

        fun durationRemaining(durationRemaining: Double) =
            apply { this.durationRemaining = durationRemaining }

        fun fractionTraveled(fractionTraveled: Float) =
            apply { this.fractionTraveled = fractionTraveled }

        fun currentStepProgress(currentStepProgress: RouteStepProgressNavigation) =
            apply { this.currentStepProgress = currentStepProgress }

        fun upcomingStep(upComingStep: LegStep) =
            apply { this.upComingStep = upComingStep }

        fun build(): RouteLegProgressNavigation {
            distanceTraveled = distanceTraveled()
            fractionTraveled = fractionTraveled(distanceTraveled)
            previousStep = previousStep()
            upComingStep = upComingStep()
            followOnStep = followOnStep()
            currentStepProgress = RouteStepProgressNavigation.Builder()
                .step(_currentStep)
                .distanceRemaining(stepDistanceRemaining)
                .build()

            validate()

            return RouteLegProgressNavigation(
                stepIndex,
                distanceTraveled,
                distanceRemaining,
                durationRemaining,
                fractionTraveled,
                _currentStep,
                previousStep,
                upComingStep,
                followOnStep,
                currentStepProgress,
                currentStepPoints,
                upcomingStepPoints,
                _routeLeg,
                stepDistanceRemaining,
                this
            )
        }

        private fun distanceTraveled(): Double =
            _routeLeg.distance()?.let { distance ->
                return when (distance - distanceRemaining < 0) {
                    true -> {
                        0.0
                    }
                    else -> {
                        distance - distanceRemaining
                    }
                }
            } ?: distanceRemaining

        private fun fractionTraveled(distanceTraveled: Double): Float {
            if (distanceTraveled == 0.0) {
                return 1.0f
            }
            return _routeLeg.distance()?.let { distance ->
                when (distance > 0) {
                    true -> {
                        (distanceTraveled / distance).toFloat()
                    }
                    else -> {
                        1.0f
                    }
                }
            } ?: 1.0f
        }

        private fun previousStep(): LegStep? =
            ifNonNull(_routeLeg.steps()) { routeLegSteps ->
                return when {
                    stepIndex != 0 -> routeLegSteps[stepIndex - 1]
                    else -> null
                }
            }

        private fun upComingStep(): LegStep? =
            ifNonNull(_routeLeg.steps()) { routeLegSteps ->
                return when {
                    routeLegSteps.size - 1 > stepIndex -> routeLegSteps[stepIndex + 1]
                    else -> null
                }
            }

        private fun followOnStep(): LegStep? =
            ifNonNull(_routeLeg.steps()) { routeLegSteps ->
                return when {
                    routeLegSteps.size - 2 > stepIndex -> routeLegSteps[stepIndex + 2]
                    else -> null
                }
            }
    }
}
