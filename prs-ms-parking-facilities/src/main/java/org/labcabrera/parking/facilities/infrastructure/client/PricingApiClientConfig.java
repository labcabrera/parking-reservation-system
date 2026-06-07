package org.labcabrera.parking.facilities.infrastructure.client;

import org.labcabrera.parking.facilities.generated.client.pricing.ApiClient;
import org.labcabrera.parking.facilities.generated.client.pricing.api.PricingApi;
import org.labcabrera.parking.facilities.generated.client.pricing.api.PricingRulesApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PricingApiClientConfig {

    @Value("${facilities.clients.pricing.url:http://localhost:8083}")
    private String pricingServiceUrl;

    @Bean
    public ApiClient pricingApiClient() {
        ApiClient apiClient = new ApiClient(ApiClient.buildRestClient());
        apiClient.setBasePath(pricingServiceUrl);
        return apiClient;
    }

    @Bean
    public PricingApi pricingApi(ApiClient pricingApiClient) {
        return new PricingApi(pricingApiClient);
    }

    @Bean
    public PricingRulesApi pricingRulesApi(ApiClient pricingApiClient) {
        return new PricingRulesApi(pricingApiClient);
    }
}
