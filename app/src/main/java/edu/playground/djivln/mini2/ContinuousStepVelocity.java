package edu.playground.djivln.mini2;

final class ContinuousStepVelocity {
    private static final double MINIMUM_THROUGH_SPEED_METERS_PER_SECOND = 0.20;
    private static final double DIRECTION_EPSILON = 1.0e-6;

    private ContinuousStepVelocity() {}

    static double[] preserveHorizontalMomentum(
            double forward,
            double right,
            double measuredHorizontalSpeed,
            double speedLimit,
            boolean hasFollowingStep) {
        if (!hasFollowingStep || !Double.isFinite(forward) || !Double.isFinite(right)
                || !Double.isFinite(measuredHorizontalSpeed) || !Double.isFinite(speedLimit)
                || speedLimit <= 0.0) {
            return new double[] {forward, right};
        }

        double commandSpeed = Math.hypot(forward, right);
        if (commandSpeed <= DIRECTION_EPSILON) return new double[] {forward, right};

        // A queued action is already available. Do not let the proportional
        // position controller brake at this intermediate waypoint: retain the
        // aircraft's measured momentum (bounded by the user's selected limit).
        double throughSpeed = Math.min(speedLimit,
                Math.max(MINIMUM_THROUGH_SPEED_METERS_PER_SECOND, measuredHorizontalSpeed));
        if (commandSpeed >= throughSpeed) return new double[] {forward, right};

        double scale = throughSpeed / commandSpeed;
        return new double[] {forward * scale, right * scale};
    }
}
