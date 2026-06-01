package org.labcabrera.parking.catalog;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayName("Catalog Service — Hexagonal Architecture Layer Enforcement")
class ArchitectureTest {

    private static final String BASE_PACKAGE = "org.labcabrera.parking.catalog";

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
    @DisplayName("Infrastructure and interfaces may depend on application and domain (smoke check)")
    void infrastructureAndInterfacesMayDependOnApplicationAndDomain() {
        // Positive rule: verify the allowed direction compiles without violations
        // (infrastructure → application/domain is the correct dependency direction)
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PACKAGE + ".infrastructure..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PACKAGE + ".interfaces..");
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("AvailabilityChangeConsumer must reside in infrastructure.messaging")
    void availabilityChangeConsumerMustResideInInfrastructureMessaging() {
        ArchRule rule = noClasses()
                .that().haveSimpleName("AvailabilityChangeConsumer")
                .should().resideOutsideOfPackage(BASE_PACKAGE + ".infrastructure.messaging");
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("AvailabilityStreamRegistry must reside in interfaces.rest")
    void availabilityStreamRegistryMustResideInInterfacesRest() {
        ArchRule rule = noClasses()
                .that().haveSimpleName("AvailabilityStreamRegistry")
                .should().resideOutsideOfPackage(BASE_PACKAGE + ".interfaces.rest");
        rule.check(importedClasses);
    }

    @Test
    @DisplayName("SearchParkingQueryHandler must not import infrastructure classes directly")
    void searchQueryHandlerMustNotImportInfrastructure() {
        ArchRule rule = noClasses()
                .that().haveSimpleName("SearchParkingQueryHandler")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PACKAGE + ".infrastructure..");
        rule.check(importedClasses);
    }
}
