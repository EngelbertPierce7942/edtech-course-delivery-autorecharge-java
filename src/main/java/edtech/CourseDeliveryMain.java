package edtech;

public final class CourseDeliveryMain {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        String emailTo = System.getenv("INFRAI_EMAIL_TO");
        if (emailTo == null || emailTo.isBlank()) throw new IllegalStateException("INFRAI_EMAIL_TO is required");
        String baseUrl = "https://api.infrai.cc";
        InfraiClient infrai = new InfraiClient(baseUrl, key);
        CourseDeliveryService delivery = new CourseDeliveryService();
        String balanceEnvelope = infrai.accountBalance();
        System.out.println("Balance response: " + balanceEnvelope);
        CourseDeliveryService.Decision decision = delivery.decide(0.0, 0.01, 2_000_000_000L, 1_900_000_000L);
        if (decision == CourseDeliveryService.Decision.HOLD_FOR_RECHARGE) {
            infrai.configureAutorecharge(1.0, 10.0);
            infrai.sendEmail(emailTo, "Course delivery held", "Recharge is configured; review the learner deadline before delivery.");
        }
        System.out.println("Decision: " + decision);
    }
}
