package com.multi.shop.api.multi_shop_api;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import java.util.List;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = ModuleBoundariesTest.ROOT, importOptions = ImportOption.DoNotIncludeTests.class)
class ModuleBoundariesTest {

    static final String ROOT = "com.multi.shop.api.multi_shop_api";
    private static final List<String> MODULES = List.of("identity", "catalog", "transactions");

    @ArchTest
    static final ArchRule catalogUsesNoOtherModule = module("catalog");

    @ArchTest
    static final ArchRule identityUsesNoOtherModule = module("identity");

    @ArchTest
    static final ArchRule transactionsOnlyUsesCatalogAndIdentityApis = module("transactions", "catalog", "identity");

    @ArchTest
    static final ArchRule commonUsesNoModule = noClasses()
        .that().resideInAPackage(ROOT + ".common..")
        .should().dependOnClassesThat(anyModule())
        .because("common is shared by every module and must not know any of them");

    @ArchTest
    static final ArchRule appOnlyUsesModuleApis = noClasses()
        .that().resideInAPackage(ROOT + ".app..")
        .should().dependOnClassesThat(anyModule().and(not(resideInAPackage("..api.."))))
        .because("app joins modules only through their public api");

    @ArchTest
    static final ArchRule apisDoNotExposeInternals = noClasses()
        .that().resideInAPackage(ROOT + ".*.api..")
        .should().dependOnClassesThat(resideInAnyPackage("..entities..", "..repositories..", "..services..", "..mappers.."))
        .because("an api only shares records and interfaces, never entities or implementations");

    @ArchTest
    static final ArchRule tokensAreOnlyCheckedWithThePublicKey = noClasses()
        .should().callMethod(io.jsonwebtoken.JwtBuilder.class, "signWith", java.security.Key.class)
        .because("only identity-service signs tokens");

    @ArchTest
    static final ArchRule cloudinaryLivesInTheMediaService = noClasses()
        .should().dependOnClassesThat().resideInAPackage("com.cloudinary..")
        .because("only media-service talks to Cloudinary");

    @ArchTest
    static final ArchRule extractedModulesOnlyKeepTheirClient = classes()
        .that().resideInAnyPackage(ROOT + ".catalog..", ROOT + ".identity..")
        .should().resideInAnyPackage(ROOT + ".*.api..", ROOT + ".*.client..")
        .because("catalog and identity live in their own services; the monolith only keeps how to call them");

    private static ArchRule module(String name, String... allowedApis) {
        List<String> allowed = List.of(allowedApis);

        DescribedPredicate<JavaClass> forbidden = MODULES.stream()
            .filter(other -> !other.equals(name))
            .<DescribedPredicate<JavaClass>>map(other -> allowed.contains(other)
                ? resideInAPackage(ROOT + "." + other + "..").and(not(resideInAPackage(ROOT + "." + other + ".api..")))
                : resideInAPackage(ROOT + "." + other + ".."))
            .reduce((first, second) -> first.or(second))
            .orElseThrow();

        return noClasses()
            .that().resideInAPackage(ROOT + "." + name + "..")
            .should().dependOnClassesThat(forbidden)
            .because(allowed.isEmpty()
                ? name + " does not need any other module"
                : name + " may only use the api of " + String.join(" and ", allowed));
    }

    private static DescribedPredicate<JavaClass> anyModule() {
        return MODULES.stream()
            .<DescribedPredicate<JavaClass>>map(module -> resideInAPackage(ROOT + "." + module + ".."))
            .reduce((first, second) -> first.or(second))
            .orElseThrow();
    }
}
