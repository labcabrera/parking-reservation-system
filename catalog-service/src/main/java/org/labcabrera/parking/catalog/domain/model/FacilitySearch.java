package org.labcabrera.parking.catalog.domain.model;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.labcabrera.parking.catalog.application.cqrs.command.CreateFacilitySearchCommand;

@Aggregate
public class FacilitySearch {

    @AggregateIdentifier
    private String id;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiration;
    private String searchQuery;
    private Set<String> features;

    @CommandHandler
    public FacilitySearch(CreateFacilitySearchCommand command) {
        this.id = UUID.randomUUID().toString();
        this.status = "CREATED";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.expiration = this.createdAt.plusMinutes(10);
        this.searchQuery = command.searchQuery();
        this.features = command.features();
    }
}
