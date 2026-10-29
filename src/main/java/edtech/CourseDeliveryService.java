package edtech;

public final class CourseDeliveryService {
    public enum Decision { DELIVER, HOLD_FOR_RECHARGE }

    public Decision decide(double balanceUsd, double requiredUsd, long deadlineEpochSeconds, long nowEpochSeconds) {
        boolean deadlineOpen = deadlineEpochSeconds >= nowEpochSeconds;
        return deadlineOpen && balanceUsd >= requiredUsd ? Decision.DELIVER : Decision.HOLD_FOR_RECHARGE;
    }
}
