package org.labcabrera.parking.facilities.domain.valueobject;

import jakarta.validation.constraints.Min;

public record CancellationPolicy(

    @Min(0) int freeCancelHours,

    @Min(0) int penaltyCancelMinutes) {

}
