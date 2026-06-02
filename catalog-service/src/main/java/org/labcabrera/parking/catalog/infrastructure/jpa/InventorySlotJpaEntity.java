package org.labcabrera.parking.catalog.infrastructure.jpa;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "inventory_slot",
    schema = "catalog",
    uniqueConstraints = @UniqueConstraint(name = "uk_inventory_slot", columnNames = {"facility_id", "slot_start"}),
    indexes = @Index(name = "ix_inventory_slot_facility_start", columnList = "facility_id, slot_start"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventorySlotJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "facility_id", nullable = false)
    private UUID facilityId;

    @Column(name = "slot_start", nullable = false)
    private LocalDateTime slotStart;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int reserved;

    @Version
    private Long version;
}
