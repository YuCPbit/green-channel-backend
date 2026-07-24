package edu.greenchannel.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class GatewayServiceApplicationTest {

    @Autowired
    private Environment environment;

    @Autowired
    @Qualifier("gatewayCorsConfigurationSource")
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void contextLoadsWithConfiguredRoutes() {
        assertEquals("gift-service", routeId(0));
        assertEquals("subsidy-service", routeId(1));
        assertEquals("tutor-affair-service", routeId(2));
        assertEquals("workstudy-service", routeId(3));
        assertEquals("dashboard-service", routeId(4));
        assertEquals("platform-service", routeId(5));
        assertEquals("Path=/api/**", environment.getProperty(routeKey(5) + ".predicates[0]"));
    }

    @Test
    void gatewayOwnsFrontendCorsConfiguration() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader(HttpHeaders.ORIGIN, "http://localhost:5173");
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
        MockHttpServletResponse response = new MockHttpServletResponse();

        CorsConfiguration configuration = corsConfigurationSource.getCorsConfiguration(request);

        assertNotNull(configuration);
        assertEquals(java.util.List.of("http://localhost:5173"), configuration.getAllowedOrigins());
        assertTrue(configuration.getAllowedMethods().contains("OPTIONS"));
        assertTrue(configuration.getAllowedHeaders().contains("*"));
        assertEquals(Boolean.TRUE, configuration.getAllowCredentials());

        new CorsFilter(corsConfigurationSource).doFilter(request, response, new MockFilterChain());
        assertEquals("http://localhost:5173", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        assertEquals("true", response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    private String routeId(int index) {
        return environment.getProperty(routeKey(index) + ".id");
    }

    private String routeKey(int index) {
        return "spring.cloud.gateway.server.webmvc.routes[" + index + "]";
    }
}
