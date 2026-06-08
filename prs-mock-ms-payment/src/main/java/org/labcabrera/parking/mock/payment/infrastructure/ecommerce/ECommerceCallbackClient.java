package org.labcabrera.parking.mock.payment.infrastructure.ecommerce;

import org.labcabrera.parking.mock.payment.interfaces.rest.dto.ECommercePaymentCallbackRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ECommerceCallbackClient {

    private final RestClient restClient;

    public ECommerceCallbackClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public void notify(String callbackUrl, ECommercePaymentCallbackRequest request) {
        restClient.post()
            .uri(callbackUrl)
            .body(request)
            .retrieve()
            .toBodilessEntity();
    }
}
