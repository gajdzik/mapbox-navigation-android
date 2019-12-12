package com.mapbox.navigation.ui.instruction.turnlane;

import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants;

class TurnLaneViewData {

  static final String DRAW_LANE_SLIGHT_RIGHT = "draw_lane_slight_right";
  static final String DRAW_LANE_RIGHT = "draw_lane_right";
  static final String DRAW_LANE_STRAIGHT = "draw_lane_straight";
  static final String DRAW_LANE_UTURN = "draw_lane_uturn";
  static final String DRAW_LANE_RIGHT_ONLY = "draw_lane_right_only";
  static final String DRAW_LANE_STRAIGHT_ONLY = "draw_lane_straight_only";

  private boolean shouldFlip;
  private String drawMethod;

  TurnLaneViewData(String laneIndications, String maneuverModifier) {
    buildDrawData(laneIndications, maneuverModifier);
  }

  boolean shouldBeFlipped() {
    return shouldFlip;
  }

  String getDrawMethod() {
    return drawMethod;
  }

  private void buildDrawData(String laneIndications, String maneuverModifier) {

    // U-turn
    if (laneIndications.contentEquals(NavigationConstants.TURN_LANE_INDICATION_UTURN)) {
      drawMethod = DRAW_LANE_UTURN;
      shouldFlip = true;
      return;
    }

    // Straight
    if (laneIndications.contentEquals(NavigationConstants.TURN_LANE_INDICATION_STRAIGHT)) {
      drawMethod = DRAW_LANE_STRAIGHT;
      return;
    }

    // Right or left
    if (laneIndications.contentEquals(NavigationConstants.TURN_LANE_INDICATION_RIGHT)) {
      drawMethod = DRAW_LANE_RIGHT;
      return;
    } else if (laneIndications.contentEquals(NavigationConstants.TURN_LANE_INDICATION_LEFT)) {
      drawMethod = DRAW_LANE_RIGHT;
      shouldFlip = true;
      return;
    }

    // Slight right or slight left
    if (laneIndications.contentEquals(NavigationConstants.TURN_LANE_INDICATION_SLIGHT_RIGHT)) {
      drawMethod = DRAW_LANE_SLIGHT_RIGHT;
      return;
    } else if (laneIndications.contentEquals(NavigationConstants.TURN_LANE_INDICATION_SLIGHT_LEFT)) {
      drawMethod = DRAW_LANE_SLIGHT_RIGHT;
      shouldFlip = true;
      return;
    }

    // Straight and right or left
    if (isStraightPlusIndication(laneIndications, NavigationConstants.TURN_LANE_INDICATION_RIGHT)) {
      setDrawMethodWithModifier(maneuverModifier);
    } else if (isStraightPlusIndication(laneIndications, NavigationConstants.TURN_LANE_INDICATION_LEFT)) {
      setDrawMethodWithModifier(maneuverModifier);
      shouldFlip = true;
    }
  }

  private void setDrawMethodWithModifier(String maneuverModifier) {
    if (maneuverModifier.contains(NavigationConstants.STEP_MANEUVER_MODIFIER_RIGHT)) {
      drawMethod = DRAW_LANE_RIGHT_ONLY;
    } else if (maneuverModifier.contains(NavigationConstants.STEP_MANEUVER_MODIFIER_STRAIGHT)) {
      drawMethod = DRAW_LANE_STRAIGHT_ONLY;
    } else {
      drawMethod = DRAW_LANE_RIGHT_ONLY;
    }
  }

  private boolean isStraightPlusIndication(String laneIndications, String turnLaneIndication) {
    return laneIndications.contains(NavigationConstants.TURN_LANE_INDICATION_STRAIGHT)
      && laneIndications.contains(turnLaneIndication);
  }
}
