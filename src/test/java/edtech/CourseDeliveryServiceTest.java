package edtech;

public final class CourseDeliveryServiceTest {
    public static void main(String[] args) {
        CourseDeliveryService service = new CourseDeliveryService();
        require(service.decide(2.0, 1.0, 100L, 90L) == CourseDeliveryService.Decision.DELIVER);
        require(service.decide(0.5, 1.0, 100L, 90L) == CourseDeliveryService.Decision.HOLD_FOR_RECHARGE);
        require(service.decide(2.0, 1.0, 80L, 90L) == CourseDeliveryService.Decision.HOLD_FOR_RECHARGE);
        System.out.println("Course delivery decisions passed");
    }

    private static void require(boolean condition) {
        if (!condition) throw new AssertionError("business decision did not match expectation");
    }
}
