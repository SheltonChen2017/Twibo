package edu.miis.trading;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

class TradingAgentClientTests {
    @Test
    void disabledIntegrationDoesNotMakeARequest() {
        TradingAgentClient client = new TradingAgentClient(
                RestClient.builder(), "", "", Duration.ofSeconds(1), Duration.ofSeconds(1));

        TradingAgentClient.Dashboard dashboard = client.loadDashboard(7L, true);

        assertThat(dashboard.configured()).isFalse();
        assertThat(dashboard.available()).isFalse();
        assertThat(dashboard.briefing()).isNull();
    }

    @Test
    void loadsReadOnlyBriefingWithBearerAuthentication() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://agent.internal")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer test-token");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TradingAgentClient client = new TradingAgentClient(builder.build());
        server.expect(once(), requestTo("https://agent.internal/v1/me/briefing"))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(header("X-Twibo-User-Id", "42"))
                .andRespond(withSuccess("""
                        {
                          "generated_at": "2026-07-28T00:00:00Z",
                          "schema_version": "2.0",
                          "portfolio": {
                            "positions": [{
                              "ticker": "QQQ",
                              "shares": 2,
                              "current_price": 500,
                              "market_value": 1000,
                              "unrealized_pnl_pct": 5.5,
                              "is_leveraged_etf": false
                            }],
                            "cash": 250,
                            "total_equity": 1250,
                            "as_of": "2026-07-27",
                            "source": "alpaca",
                            "account_mode": "paper"
                          },
                          "risk": {
                            "basket_exposure_pct": {"broad_market": 80},
                            "leveraged_etf_exposure_pct": 0,
                            "cash_pct": 20,
                            "largest_single_position_pct": 80,
                            "concentration_warnings": []
                          },
                          "regime": {
                            "benchmark_ticker": "QQQ",
                            "trend": "uptrend",
                            "volatility_regime": "low_vol",
                            "trailing_volatility_pct": 14,
                            "as_of": "2026-07-27"
                          },
                          "signals": [],
                          "warnings": [],
                          "analytics": {},
                          "data_freshness": {"portfolio_as_of": "2026-07-27"}
                        }
                        """, MediaType.APPLICATION_JSON));

        TradingAgentClient.Dashboard dashboard = client.loadDashboard(42L, true);

        assertThat(dashboard.available()).isTrue();
        assertThat(dashboard.briefing().portfolio().positions()).hasSize(1);
        assertThat(dashboard.briefing().portfolio().positions().getFirst().ticker()).isEqualTo("QQQ");
        assertThat(dashboard.briefing().risk().cashPct()).isEqualByComparingTo("20");
        server.verify();
    }

    @Test
    void doesNotContactAgentWithoutUserOwnedBrokerConnection() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        TradingAgentClient client = new TradingAgentClient(builder.build());

        TradingAgentClient.Dashboard dashboard = client.loadDashboard(42L, false);

        assertThat(dashboard.available()).isFalse();
        assertThat(dashboard.message()).contains("No connected Alpaca paper account");
        server.verify();
    }
}
