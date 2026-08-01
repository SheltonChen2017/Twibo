package edu.miis.trading;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class TradingAgentClient {
    private final RestClient restClient;
    private final String configurationError;

    @Autowired
    public TradingAgentClient(
            RestClient.Builder builder,
            @Value("${trading-agent.base-url:}") String baseUrl,
            @Value("${trading-agent.api-token:}") String apiToken,
            @Value("${trading-agent.connect-timeout:2s}") Duration connectTimeout,
            @Value("${trading-agent.read-timeout:8s}") Duration readTimeout) {
        String normalizedUrl = baseUrl == null ? "" : baseUrl.trim();
        String normalizedToken = apiToken == null ? "" : apiToken.trim();

        if (normalizedUrl.isEmpty() && normalizedToken.isEmpty()) {
            restClient = null;
            configurationError = "Trading Agent is not configured.";
            return;
        }
        if (normalizedUrl.isEmpty() || normalizedToken.isEmpty()) {
            restClient = null;
            configurationError = "Trading Agent configuration is incomplete.";
            return;
        }

        URI uri;
        try {
            uri = URI.create(normalizedUrl);
        } catch (IllegalArgumentException ex) {
            restClient = null;
            configurationError = "Trading Agent URL is invalid.";
            return;
        }
        if (uri.getHost() == null
                || (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()))
                || uri.getUserInfo() != null
                || uri.getQuery() != null
                || uri.getFragment() != null) {
            restClient = null;
            configurationError = "Trading Agent URL must be an HTTP(S) service URL without credentials, query, or fragment.";
            return;
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        restClient = builder
                .baseUrl(stripTrailingSlash(normalizedUrl))
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + normalizedToken)
                .build();
        configurationError = null;
    }

    TradingAgentClient(RestClient restClient) {
        this.restClient = restClient;
        this.configurationError = null;
    }

    public Dashboard loadDashboard(Long userId, boolean brokerConnected) {
        if (!brokerConnected) {
            return new Dashboard(
                    restClient != null,
                    false,
                    "No connected Alpaca paper account is registered for this Twibo user.",
                    null);
        }
        if (restClient == null) {
            return new Dashboard(false, false, configurationError, null);
        }
        try {
            DecisionPacket packet = restClient.get()
                    .uri("/v1/me/briefing")
                    .header("X-Twibo-User-Id", userId.toString())
                    .retrieve()
                    .body(DecisionPacket.class);
            if (packet == null) {
                return new Dashboard(true, false, "Trading Agent returned an empty briefing.", null);
            }
            return new Dashboard(true, true, null, packet);
        } catch (RestClientException ex) {
            return new Dashboard(
                    true,
                    false,
                    "Trading Agent is temporarily unavailable. Twibo remains fully usable.",
                    null);
        }
    }

    private static String stripTrailingSlash(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }

    public record Dashboard(boolean configured, boolean available, String message, DecisionPacket briefing) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DecisionPacket(
            @JsonProperty("generated_at") String generatedAt,
            @JsonProperty("schema_version") String schemaVersion,
            Portfolio portfolio,
            Risk risk,
            Regime regime,
            List<Signal> signals,
            List<String> warnings,
            Map<String, Object> analytics,
            @JsonProperty("data_freshness") Map<String, String> dataFreshness) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Portfolio(
            List<Position> positions,
            BigDecimal cash,
            @JsonProperty("total_equity") BigDecimal totalEquity,
            @JsonProperty("as_of") String asOf,
            String source,
            @JsonProperty("account_mode") String accountMode) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Position(
            String ticker,
            BigDecimal shares,
            @JsonProperty("current_price") BigDecimal currentPrice,
            @JsonProperty("market_value") BigDecimal marketValue,
            @JsonProperty("unrealized_pnl_pct") BigDecimal unrealizedPnlPct,
            @JsonProperty("is_leveraged_etf") boolean leveragedEtf) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Risk(
            @JsonProperty("basket_exposure_pct") Map<String, BigDecimal> basketExposurePct,
            @JsonProperty("leveraged_etf_exposure_pct") BigDecimal leveragedEtfExposurePct,
            @JsonProperty("cash_pct") BigDecimal cashPct,
            @JsonProperty("largest_single_position_pct") BigDecimal largestSinglePositionPct,
            @JsonProperty("concentration_warnings") List<String> concentrationWarnings) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Regime(
            @JsonProperty("benchmark_ticker") String benchmarkTicker,
            String trend,
            @JsonProperty("volatility_regime") String volatilityRegime,
            @JsonProperty("trailing_volatility_pct") BigDecimal trailingVolatilityPct,
            @JsonProperty("as_of") String asOf) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Signal(
            String label,
            String claim,
            String status,
            String detail,
            String source,
            @JsonProperty("relevant_tickers") List<String> relevantTickers) {}
}
