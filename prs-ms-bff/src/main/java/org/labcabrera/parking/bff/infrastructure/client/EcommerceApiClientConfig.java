package org.labcabrera.parking.bff.infrastructure.client;

import org.labcabrera.parking.bff.generated.client.ecommerce.ApiClient;
import org.labcabrera.parking.bff.generated.client.ecommerce.api.PaymentMethodsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class EcommerceApiClientConfig {

    @Value("${bff.clients.ecommerce.url}")
    private String ecommerceServiceUrl;

    @Bean
    public ApiClient ecommerceApiClient() {
        ApiClient apiClient = new ApiClient(RestClient.builder().build());
        apiClient.setBasePath(ecommerceServiceUrl);
        return apiClient;
    }

    @Bean
    public PaymentMethodsApi paymentMethodsApi(ApiClient ecommerceApiClient) {
        return new PaymentMethodsApi(ecommerceApiClient);
    }
}
