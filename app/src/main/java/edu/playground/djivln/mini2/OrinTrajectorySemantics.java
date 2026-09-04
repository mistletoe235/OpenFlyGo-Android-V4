package edu.playground.djivln.mini2;

final class OrinTrajectorySemantics {
    private static final double MIN_YAW_DISPLACEMENT_METERS = 0.05;
    private static final double MAX_YAW_RATE_DEGREES_PER_SECOND = 20.0;
    private static final double MAX_YAW_ACCEL_DEGREES_PER_SECOND_SQUARED = 45.0;
    private static final double FLY_THROUGH_MIN_RADIUS_METERS = 0.12;
    private static final double FLY_THROUGH_MAX_RADIUS_METERS = 0.8;
    private static final double FLY_THROUGH_RADIUS_RATIO = 0.25;

    private OrinTrajectorySemantics() {}

    static double yawDeltaDegrees(double forwardMeters, double rightMeters) {
        if (!Double.isFinite(forwardMeters) || !Double.isFinite(rightMeters)) return 0.0;
        if (Math.hypot(forwardMeters, rightMeters) < MIN_YAW_DISPLACEMENT_METERS
                || Math.abs(forwardMeters) <= 1.0e-6) {
            return 0.0;
        }
        return Math.toDegrees(Math.atan2(rightMeters, forwardMeters));
    }

    static double targetHeadingDegrees(
            double startHeadingDegrees,
            double forwardMeters,
            double rightMeters) {
        return wrapDegrees(startHeadingDegrees + yawDeltaDegrees(forwardMeters, rightMeters));
    }

    static double yawRateDegreesPerSecond(
            double currentHeadingDegrees,
            double targetHeadingDegrees,
            double previousYawRateDegreesPerSecond,
            double seconds) {
        double desired = clamp(
                wrapDegrees(targetHeadingDegrees - currentHeadingDegrees),
                -MAX_YAW_RATE_DEGREES_PER_SECOND,
                MAX_YAW_RATE_DEGREES_PER_SECOND);
        double maxStep = MAX_YAW_ACCEL_DEGREES_PER_SECOND_SQUARED * Math.max(0.0, seconds);
        return previousYawRateDegreesPerSecond
                + clamp(desired - previousYawRateDegreesPerSecond, -maxStep, maxStep);
    }

    static double flyThroughRadiusMeters(double segmentDistanceMeters) {
        return Math.max(
                FLY_THROUGH_MIN_RADIUS_METERS,
                Math.min(FLY_THROUGH_MAX_RADIUS_METERS,
                        Math.max(0.0, segmentDistanceMeters) * FLY_THROUGH_RADIUS_RATIO));
    }

    static long flyThroughTimeoutMillis(double segmentDistanceMeters) {
        return Math.round((8.0 + Math.max(0.0, segmentDistanceMeters) * 4.0) * 1_000.0);
    }

    static long finalPositionTimeoutMillis(
            double horizontalDistanceMeters,
            double verticalDistanceMeters) {
        double travelSeconds = Math.max(
                Math.max(0.0, horizontalDistanceMeters) / 0.7,
                Math.abs(verticalDistanceMeters) / 0.14);
        return Math.min(20_000L, Math.max(5_000L, (long) (2_000.0 + travelSeconds * 1_000.0)));
    }

    static double wrapDegrees(double value) {
        double wrapped = value;
        while (wrapped > 180.0) wrapped -= 360.0;
        while (wrapped < -180.0) wrapped += 360.0;
        return wrapped;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
