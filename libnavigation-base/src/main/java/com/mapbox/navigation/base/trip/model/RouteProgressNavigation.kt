package com.mapbox.navigation.base.trip.model

import com.mapbox.api.directions.v5.models.*
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.ifNonNull

class RouteProgressNavigation private constructor(
    private val route: DirectionsRoute? = null,
    private val routeGeometryWithBuffer: Geometry? = null,
    private val bannerInstructions: BannerInstructions? = null,
    private val voiceInstructions: VoiceInstructions? = null,
    private val currentState: RouteProgressStateNavigation? = null,
    private val currentLegProgress: RouteLegProgressNavigation? = null,
    private val upcomingStepPoints: List<Point>? = null,
    private val inTunnel: Boolean = false,
    private val distanceRemaining: Double = 0.0,
    private val distanceTraveled: Double = 0.0,
    private val durationRemaining: Long = 0L,
    private val fractionTraveled: Double = 0.0,
    private val remainingWaypoints: Int = 0,
    private val builder: Builder
) {

    /**
     * Get the route the navigation session is currently using. When a reroute occurs and a new
     * directions route gets obtained, with the next location update this directions route should
     * reflect the new route.
     *
     * @return a [DirectionsRoute] currently being used for the navigation session
     */
    fun route() = route

    /**
     * Total distance traveled in meters along route.
     *
     * @return a double value representing the total distance the user has traveled along the route,
     * using unit meters
     */
    fun distanceTraveled(): Double = distanceTraveled
    /*
    ifNonNull(route?.distance()) { distance ->
        when (distance - distanceRemaining < 0) {
            true -> {
                0.0
            }
            false -> {
                distance - distanceRemaining
            }
        }
    } ?: 0.0
*/
    /**
     * Provides the duration remaining in seconds till the user reaches the end of the route.
     *
     * @return `long` value representing the duration remaining till end of route, in unit
     * seconds
     */
    fun durationRemaining(): Long = durationRemaining
    /*
    ifNonNull(fractionTraveled(), route()?.duration()) { fractionTraveled, duration ->
        ((1 - fractionTraveled) * duration).toLong()
    } ?: 0L
*/
    /**
     * Get the fraction traveled along the current route, this is a float value between 0 and 1 and
     * isn't guaranteed to reach 1 before the user reaches the end of the route.
     *
     * @return a double value between 0 and 1 representing the fraction the user has traveled along the
     * route
     */
    private fun fractionTraveled(): Double? = fractionTraveled
    /*
    {
        if (distanceTraveled() == 0.0) {
            return 1.0
        }
        return route()?.distance()?.let { distance ->
            when (distance > 0) {
                true -> {
                    (distanceTraveled() / distance)
                }
                false -> {
                    1.0
                }
            }
        } ?: 1.0
    }
*/
    /**
     * Provides the distance remaining in meters till the user reaches the end of the route.
     *
     * @return `long` value representing the distance remaining till end of route, in unit meters
     */
    fun distanceRemaining(): Double = distanceRemaining

    /**
     * Number of waypoints remaining on the current route.
     *
     * @return integer value representing the number of way points remaining along the route
     */
    fun remainingWaypoints(): Int = remainingWaypoints

    /*
    ifNonNull(route?.legs(), currentLegProgress?.legIndex()) { legs, legIndex -> legs.size - legIndex }
        ?: 0
*/
    /**
     * Gives a [RouteLegProgressNavigation] object with information about the particular leg the user is
     * currently on.
     *
     * @return a [RouteLegProgressNavigation] object
     */
    fun currentLegProgress() = currentLegProgress

    /**
     * Provides a list of points that represent the upcoming step
     * step geometry.
     *
     * @return list of points representing the upcoming step
     */
    fun upcomingStepPoints() = upcomingStepPoints

    /**
     * Returns whether or not the location updates are
     * considered in a tunnel along the route.
     *
     * @return true if in a tunnel, false otherwise
     */
    fun inTunnel() = inTunnel

    /**
     * Current banner instruction.
     *
     * @return current banner instruction
     */
    fun bannerInstruction() = bannerInstructions

    /**
     * Current voice instruction.
     *
     * @return current voice instruction
     */
    fun voiceInstructions() = voiceInstructions

    /**
     * Returns the current state of progress along the route.  Provides route and location tracking
     * information.
     *
     * @return the current state of progress along the route.
     */
    fun currentState() = currentState

    /**
     * Returns the current [DirectionsRoute] geometry with a buffer
     * that encompasses visible tile surface are while navigating.
     *
     *
     * This [Geometry] is ideal for offline downloads of map or routing tile
     * data.
     *
     * @return current route geometry with buffer
     */
    fun routeGeometryWithBuffer() = routeGeometryWithBuffer

    fun toBuilder() = builder

    data class Builder(
        private var directionsRoute: DirectionsRoute? = null,
        private var routeGeometryWithBuffer: Geometry? = null,
        private var bannerInstruction: BannerInstructions? = null,
        private var voiceInstructions: VoiceInstructions? = null,
        private var currentState: RouteProgressStateNavigation? = null,
        private var currentLegProgress: RouteLegProgressNavigation? = null,
        private var upcomingStepPoints: List<Point>? = null,
        private var inTunnel: Boolean = false,
        private var distanceRemaining: Double = 0.0,
        private var distanceTraveled: Double = 0.0,
        private var durationRemaining: Long = 0L,
        private var fractionTraveled: Double = 0.0,
        private var remainingWaypoints: Int = 0
    ) {

        fun route(route: DirectionsRoute) =
            apply { this.directionsRoute = route }

        fun routeGeometryWithBuffer(routeGeometryWithBuffer: Geometry?) =
            apply { this.routeGeometryWithBuffer = routeGeometryWithBuffer }

        fun bannerInstruction(bannerInstruction: BannerInstructions?) =
            apply { this.bannerInstruction = bannerInstruction }

        fun voiceInstructions(voiceInstructions: VoiceInstructions?) =
            apply { this.voiceInstructions = voiceInstructions }

        fun currentState(currentState: RouteProgressStateNavigation) =
            apply { this.currentState = currentState }

        fun currentLegProgress(legProgressNavigation: RouteLegProgressNavigation) =
            apply { this.currentLegProgress = legProgressNavigation }

        fun upcomingStepPoints(upcomingStepPoints: List<Point>?) =
            apply { this.upcomingStepPoints = upcomingStepPoints }

        fun inTunnel(inTunnel: Boolean) = apply { this.inTunnel = inTunnel }

        fun distanceRemaining(distanceRemaining: Double) =
            apply { this.distanceRemaining = distanceRemaining }

        fun distanceTraveled(distanceTraveled: Double) =
            apply { this.distanceTraveled = distanceTraveled }

        fun durationRemaining(durationRemaining: Long) =
            apply { this.durationRemaining = durationRemaining }

        fun fractionTraveled(fractionTraveled: Double) =
            apply { this.fractionTraveled = fractionTraveled }

        fun remainingWaypoints(remainingWaypoints: Int) =
            apply { this.remainingWaypoints = remainingWaypoints }

        fun build(): RouteProgressNavigation {
            val leg: RouteLeg? = directionsRoute?.let { directionRoute ->
                directionRoute.legs()?.let { legs ->
                    legs[currentLegProgress.legIndex()]
                }
            }
            val routeLegProgressBuilder = RouteLegProgressNavigation.Builder()
            ifNonNull(leg) {
                routeLegProgressBuilder.routeLeg(it)
            }
            ifNonNull(currentStep) {
                routeLegProgressBuilder.currentStep(it)
            }
            val legProgress = routeLegProgressBuilder
                .stepIndex(stepIndex)
                .distanceRemaining(legDistanceRemaining)
                .durationRemaining(legDurationRemaining)
                .stepDistanceRemaining(stepDistanceRemaining)
                .upcomingStepPoints(upcomingStepPoints)
                .build()
            this.currentLegProgress = legProgress
            validate()

            return RouteProgressNavigation(
                directionsRoute,
                routeGeometryWithBuffer,
                bannerInstruction,
                voiceInstructions,
                currentState,
                currentLegProgress,
                upcomingStepPoints,
                inTunnel,
                distanceRemaining,
                distanceTraveled,
                durationRemaining,
                fractionTraveled,
                remainingWaypoints,
                this
            )
        }
    }
}
