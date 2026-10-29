package edtech;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfraiClient {
    // Capability name used by the account control-plane call.
    private static final String AUTORECHARGE_CAPABILITY = "account.autorecharge.configure";
    private final HttpClient http = HttpClient.newHttpClient();
    private final String baseUrl;
    private final String key;

    public InfraiClient(String baseUrl, String key) {
        this.baseUrl = baseUrl;
        this.key = key;
    }

    public String accountBalance() throws IOException, InterruptedException {
        return request("GET", "/v1/account/balance", "");
    }

    public String configureAutorecharge(double triggerBalance, double rechargeAmount) throws IOException, InterruptedException {
        String body = "{\"trigger_balance\":" + triggerBalance + ",\"recharge_amount\":" + rechargeAmount + "}";
        return request("PUT", "/v1/account/autorecharge/configure", body);
    }

    public String sendEmail(String to, String subject, String body) throws IOException, InterruptedException {
        String json = "{\"to\":\"" + json(to) + "\",\"subject\":\"" + json(subject) + "\",\"body\":\"" + json(body) + "\"}";
        return request("POST", "/v1/email/send", json);
    }

    private String request(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .method(method, body.isEmpty() ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        String envelope = response.body();
        Matcher ok = Pattern.compile("\\\"ok\\\"\\s*:\\s*(true|false)").matcher(envelope);
        if (!ok.find() || !"true".equals(ok.group(1))) {
            throw new IllegalStateException("Infrai request rejected: " + envelope);
        }
        return envelope;
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
