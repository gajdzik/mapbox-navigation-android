package com.mapbox.navigation.navigator

import android.location.Location
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.LegStep
import com.mapbox.api.directions.v5.models.RouteLeg
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.navigation.base.extensions.ifNonNull
import com.mapbox.navigation.base.trip.model.RouteLegProgressNavigation
import com.mapbox.navigation.base.trip.model.RouteProgressNavigation
import com.mapbox.navigation.base.trip.model.RouteStepProgressNavigation
import com.mapbox.navigator.*
import java.util.*
import kotlin.math.roundToLong

object MapboxNativeNavigatorImpl : MapboxNativeNavigator {

    private val navigator: Navigator = Navigator()
    private var route: DirectionsRoute? = null

    init {
        System.loadLibrary("navigator-android")
    }

    // Route following

    override fun updateLocation(rawLocation: Location): Boolean =
        navigator.updateLocation(rawLocation.toFixLocation())

    override fun getStatus(date: Date): TripStatus {
        val status = navigator.getStatus(date)
        return TripStatus(
            status.location.toLocation(),
            status.getRouteProgress()
        )
    }

    // Routing

    override fun setRoute(route: DirectionsRoute, routeIndex: Int, legIndex: Int): NavigationStatus {
        this.route = route
        return navigator.setRoute(route.toJson(), routeIndex, legIndex)
    }

    override fun updateAnnotations(
        legAnnotationJson: String,
        routeIndex: Int,
        legIndex: Int
    ): Boolean = navigator.updateAnnotations(legAnnotationJson, routeIndex, legIndex)

    override fun getBannerInstruction(index: Int): BannerInstruction? =
        navigator.getBannerInstruction(index)

    override fun getRouteGeometryWithBuffer(gridSize: Float, bufferDilation: Short): String? =
        navigator.getRouteBufferGeoJson(gridSize, bufferDilation)

    override fun updateLegIndex(routeIndex: Int, legIndex: Int): NavigationStatus =
        navigator.changeRouteLeg(routeIndex, legIndex)

    // Free Drive

    override fun getElectronicHorizon(request: String): RouterResult =
        navigator.getElectronicHorizon(request)

    // Offline

    override fun configureRouter(routerParams: RouterParams, httpClient: HttpInterface): Long =
        navigator.configureRouter(routerParams, httpClient)

    override fun getRoute(url: String): RouterResult = navigator.getRoute(url)

    override fun unpackTiles(tarPath: String, destinationPath: String): Long =
        navigator.unpackTiles(tarPath, destinationPath)

    override fun removeTiles(tilePath: String, southwest: Point, northeast: Point): Long =
        navigator.removeTiles(tilePath, southwest, northeast)

    // History traces

    override fun getHistory(): String = navigator.history

    override fun toggleHistory(isEnabled: Boolean) {
        navigator.toggleHistory(isEnabled)
    }

    override fun addHistoryEvent(eventType: String, eventJsonProperties: String) {
        navigator.pushHistory(eventType, eventJsonProperties)
    }

    // Configuration

    override fun getConfig(): NavigatorConfig = navigator.config

    override fun setConfig(config: NavigatorConfig?) {
        navigator.setConfig(config)
    }

    // Other

    override fun getVoiceInstruction(index: Int): VoiceInstruction? =
        navigator.getVoiceInstruction(index)

    private fun Location.toFixLocation() = FixLocation(
        Point.fromLngLat(this.longitude, this.latitude),
        Date(this.time),
        this.speed,
        this.bearing,
        this.altitude.toFloat(),
        this.accuracy,
        this.provider
    )

    private fun FixLocation.toLocation(): Location = Location(this.provider).also {
        it.latitude = this.coordinate.latitude()
        it.longitude = this.coordinate.longitude()
        it.time = this.time.time
        it.speed = this.speed ?: 0f
        it.bearing = this.bearing ?: 0f
        it.altitude = this.altitude?.toDouble() ?: 0.0
        it.accuracy = this.accuracyHorizontal ?: 0f
    }

    private val ONE_INDEX = 1
    private val ONE_SECOND_IN_MILLISECONDS = 1000.0
    private val FIRST_BANNER_INSTRUCTION = 0

    private fun NavigationStatus.getRouteProgress(): RouteProgressNavigation {
        val upcomingStepIndex = stepIndex + ONE_INDEX

        val routeProgressBuilder = RouteProgressNavigation.Builder()
        val legProgressBuilder = RouteLegProgressNavigation.Builder()
        val stepProgressBuilder = RouteStepProgressNavigation.Builder()

        ifNonNull(route) { route ->
            ifNonNull(route.legs()) { legs ->

                var currentLeg: RouteLeg? = null
                if (legIndex < legs.size) {
                    currentLeg = legs[legIndex]
                    legProgressBuilder.legIndex(legIndex)
                    legProgressBuilder.routeLeg(currentLeg)

                    // todo mapbox java issue - leg distance is nullable
                    val distanceTraveled = (currentLeg.distance()?.toFloat() ?: 0f) - remainingLegDistance
                    legProgressBuilder.distanceTraveled(distanceTraveled)
                    legProgressBuilder.fractionTraveled(distanceTraveled / (currentLeg.distance()?.toFloat() ?: 0f))
                }

                ifNonNull(currentLeg?.steps()) { steps ->
                    val currentStep: LegStep?
                    if (stepIndex < steps.size) {
                        currentStep = steps[stepIndex]
                        stepProgressBuilder.stepIndex(stepIndex)
                        stepProgressBuilder.step(currentStep)

                        currentStep?.distance()
                        val stepGeometry = currentStep.geometry()
                        stepGeometry?.let {
                            stepProgressBuilder.stepPoints(PolylineUtils.decode(stepGeometry, /* todo add core dependency PRECISION_6*/6))
                        }

                        val distanceTraveled = currentStep.distance().toFloat() - remainingStepDistance
                        stepProgressBuilder.distanceTraveled(distanceTraveled)
                        stepProgressBuilder.fractionTraveled(distanceTraveled / currentStep.distance().toFloat())
                    }

                    if (upcomingStepIndex < steps.size) {
                        val upcomingStep = steps[upcomingStepIndex]
                        legProgressBuilder.upcomingStep(upcomingStep)

                        val stepGeometry = upcomingStep.geometry()
                        stepGeometry?.let {
                            routeProgressBuilder.upcomingStepPoints(PolylineUtils.decode(stepGeometry, /* todo add core dependency PRECISION_6*/6))
                        }
                    }
                }
            }
        }

        stepProgressBuilder.distanceRemaining(remainingStepDistance)
        stepProgressBuilder.durationRemaining((remainingStepDuration / ONE_SECOND_IN_MILLISECONDS).roundToLong())

        legProgressBuilder.currentStepProgress(stepProgressBuilder.build())
        legProgressBuilder.distanceRemaining(remainingLegDistance)
        legProgressBuilder.durationRemaining((remainingLegDuration / ONE_SECOND_IN_MILLISECONDS).roundToLong())

        return routeProgressBuilder.build()
    }

    private fun buildRouteProgressFrom(
        status: NavigationStatus,
        navigator: MapboxNavigator
    ): RouteProgress? {

        return ifNonNull(route) { route ->
            updateSteps(route, legIndex, stepIndex)
            updateStepPoints(route, legIndex, stepIndex, upcomingStepIndex)

            val legDistanceRemaining = status.remainingLegDistance.toDouble()
            val routeDistanceRemaining = NavigationHelper.routeDistanceRemaining(
                legDistanceRemaining,
                legIndex, route
            )
            val stepDistanceRemaining = status.remainingStepDistance.toDouble()
            val legDurationRemaining = status.remainingLegDuration / ONE_SECOND_IN_MILLISECONDS

            currentLegAnnotation = ifNonNull(currentLeg) { currentLeg ->
                NavigationHelper.createCurrentAnnotation(
                    currentLegAnnotation,
                    currentLeg, legDistanceRemaining
                )
            }
            val routeState = status.routeState
            val currentRouteState = progressStateMap[routeState]

            val progressBuilder = RouteProgress.Builder()
                .distanceRemaining(routeDistanceRemaining)
                .legDistanceRemaining(legDistanceRemaining)
                .legDurationRemaining(legDurationRemaining)
                .stepDistanceRemaining(stepDistanceRemaining)
                .directionsRoute(route)
                .currentStep(currentStep)
                .currentStepPoints(currentStepPoints)
                .upcomingStepPoints(upcomingStepPoints)
                .stepIndex(stepIndex)
                .legIndex(legIndex)
                .inTunnel(status.inTunnel)
                .currentState(currentRouteState)

            addRouteGeometries(progressBuilder)
            addVoiceInstructions(status, progressBuilder)
            addBannerInstructions(status, navigator, progressBuilder)
            addUpcomingStepPoints(progressBuilder)
            progressBuilder.build()
        }
    }

    private fun updateSteps(route: DirectionsRoute, legIndex: Int, stepIndex: Int) {
        ifNonNull(route.legs()) { legs ->
            if (legIndex < legs.size) {
                currentLeg = legs[legIndex]
            }
            ifNonNull(currentLeg?.steps()) { steps ->
                if (stepIndex < steps.size) {
                    currentStep = steps[stepIndex]
                }
            }
        }
    }

    private fun updateStepPoints(
        route: DirectionsRoute,
        legIndex: Int,
        stepIndex: Int,
        upcomingStepIndex: Int
    ) {
        currentStepPoints = NavigationHelper.decodeStepPoints(
            route, currentStepPoints,
            legIndex, stepIndex
        )
        upcomingStepPoints = NavigationHelper.decodeStepPoints(
            route, null,
            legIndex, upcomingStepIndex
        )
    }

    private fun addUpcomingStepPoints(progressBuilder: RouteProgress.Builder) {
        ifNonNull(upcomingStepPoints) { upcomingStepPoints ->
            if (upcomingStepPoints.isNotEmpty())
                progressBuilder.upcomingStepPoints(upcomingStepPoints)
        }
    }

    private fun addRouteGeometries(progressBuilder: RouteProgress.Builder) {
        progressBuilder.routeGeometryWithBuffer(routeGeometryWithBuffer)
    }

    private fun addVoiceInstructions(
        status: NavigationStatus,
        progressBuilder: RouteProgress.Builder
    ) {
        val voiceInstruction = status.voiceInstruction
        progressBuilder.voiceInstruction(voiceInstruction)
    }

    private fun addBannerInstructions(
        status: NavigationStatus,
        navigator: MapboxNavigator,
        progressBuilder: RouteProgress.Builder
    ) {
        var bannerInstruction = status.bannerInstruction
        if (status.routeState == RouteState.INITIALIZED) {
            bannerInstruction = navigator.retrieveBannerInstruction(FIRST_BANNER_INSTRUCTION)
        }
        progressBuilder.bannerInstruction(bannerInstruction)
    }
}
