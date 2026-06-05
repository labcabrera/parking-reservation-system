package org.labcabrera.parking.bff.infrastructure.client;

import org.labcabrera.parking.bff.generated.client.facilities.ApiClient;
import org.labcabrera.parking.bff.generated.client.facilities.api.ParkingFacilitiesApi;
import org.labcabrera.parking.bff.generated.client.facilities.api.ReservationsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class FacilitiesApiClientConfig {

    @Value("${clients.facilities.url}")
    private String facilitiesServiceUrl;

    @Bean
    public ApiClient facilitiesApiClient() {
        ApiClient apiClient = new ApiClient(RestClient.builder().build());
        apiClient.setBasePath(facilitiesServiceUrl);
        return apiClient;
    }

    @Bean
    public ParkingFacilitiesApi parkingFacilitiesApi(ApiClient facilitiesApiClient) {
        return new ParkingFacilitiesApi(facilitiesApiClient);
    }

    @Bean
    public ReservationsApi reservationsApi(ApiClient facilitiesApiClient) {
        return new ReservationsApi(facilitiesApiClient);
    }
}
