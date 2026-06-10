package com.furnisight.admin.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class PackageArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
            .importPackages("com.furnisight.admin");

    @Test
    void legacyTechnicalLayerPackagesStayEmpty() {
        ArchRule rule = noClasses()
                .should().resideInAnyPackage(
                        "com.furnisight.admin.controller..",
                        "com.furnisight.admin.service..",
                        "com.furnisight.admin.integration..",
                        "com.furnisight.admin.entity..",
                        "com.furnisight.admin.repository..",
                        "com.furnisight.admin.config..",
                        "com.furnisight.admin.security..",
                        "com.furnisight.admin.exception..");

        rule.check(classes);
    }
}
