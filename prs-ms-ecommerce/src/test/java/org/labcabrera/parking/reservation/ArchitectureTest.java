package org.labcabrera.parking.reservation;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayName("Reservation Service — Hexagonal Architecture Layer Enforcement")
class ArchitectureTest {

    private static final String BASE_PACKAGE = "org.labcabrera.parking.reservation";

    private static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Test
    @DisplayName("Domain layer must not depend on application, infrastructure, or interfaces")
    void domainMustNotDependOnOuterLayers() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        BASE_PACKAGE + ".application..",
                        BASE_PACKAGE + ".infrastructure..",
                        BASE_PACKAGE + ".interfaces.."
                );
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Application layer must not depend on infrastructure or interfaces")
    void applicationMustNotDependOnInfrastructureOrInterfaces() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        BASE_PACKAGE + ".infrastructure..",
                        BASE_PACKAGE + ".interfaces.."
                );
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Interfaces layer must not depend on infrastructure directly")
    void interfacesMustNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".interfaces..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PACKAGE + ".infrastructure..");
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Infrastructure must not depend on interfaces")
    void infrastructureMustNotDependOnInterfaces() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".infrastructure..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PACKAGE + ".interfaces..");
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("SpotHold domain model must not import Spring or JPA classes")
    void spotHoldMustNotHaveSpringOrJpaImports() {
        ArchRule rule = noClasses()
                .that().haveSimpleName("SpotHold")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..");
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("HoldController must not import infrastructure classes directly")
    void holdControllerMustNotImportInfrastructureDirectly() {
        ArchRule rule = noClasses()
                .that().haveSimpleName("HoldController")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PACKAGE + ".infrastructure..");
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("HoldPricingCoordinator must reside in application package")
    void holdPricingCoordinatorMustResideInApplicationPackage() {
        ArchRule rule = classes()
                .that().haveSimpleName("HoldPricingCoordinator")
                .should().resideInAPackage(BASE_PACKAGE + ".application..");
        rule.check(importedClasses);
    }
}
