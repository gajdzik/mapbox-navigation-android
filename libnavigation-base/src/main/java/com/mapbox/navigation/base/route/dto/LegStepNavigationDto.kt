package com.mapbox.navigation.base.route.dto

import com.google.gson.annotations.SerializedName
import com.mapbox.navigation.base.route.model.LegStepNavigation

class LegStepNavigationDto(
    val distance: Double,
    val duration: Double,
    val geometry: String?,
    val name: String?,
    val ref: String?,
    val destinations: String?,
    val mode: String,
    val pronunciation: String?,
    @SerializedName("rotary_name")
    val rotaryName: String?,
    @SerializedName("rotary_pronunciation")
    val rotaryPronunciation: String?,
    val maneuver: StepManeuverNavigationDto,
    val voiceInstructions: List<VoiceInstructionsNavigationDto>?,
    val bannerInstructions: List<BannerInstructionsNavigationDto>?,
    @SerializedName("driving_side")
    val drivingSide: String?,
    val weight: Double,
    val intersections: List<StepIntersectionNavigationDto>?,
    val exits: String?
)

fun LegStepNavigationDto.mapToModel() = LegStepNavigation.Builder()
    .distance(distance)
    .drivingSide(drivingSide)
    .duration(duration)
    .geometry(geometry)
    .stepManeuver(maneuver.mapToModel())
    .build()
