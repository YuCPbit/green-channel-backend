package edu.greenchannel.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayServiceApplicationTest {

    @Autowired
    private Environment environment;

    @Test
    void contextLoadsWithConfiguredRoutes() {
        assertEquals("gift-service", routeId(0));
        assertEquals("subsidy-service", routeId(1));
        assertEquals("workstudy-service", routeId(2));
        assertEquals("dashboard-service", routeId(3));
        assertEquals("platform-service", routeId(4));
        assertEquals("Path=/api/**", environment.getProperty(routeKey(4) + ".predicates[0]"));
    }

    private String routeId(int index) {
        return environment.getProperty(routeKey(index) + ".id");
    }

    private String routeKey(int index) {
        return "spring.cloud.gateway.server.webmvc.routes[" + index + "]";
    }
}
