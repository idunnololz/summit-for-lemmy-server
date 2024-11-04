package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the `libs` extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final DaggerLibraryAccessors laccForDaggerLibraryAccessors = new DaggerLibraryAccessors(owner);
    private final ExposedLibraryAccessors laccForExposedLibraryAccessors = new ExposedLibraryAccessors(owner);
    private final KotlinLibraryAccessors laccForKotlinLibraryAccessors = new KotlinLibraryAccessors(owner);
    private final LogbackLibraryAccessors laccForLogbackLibraryAccessors = new LogbackLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

        /**
         * Creates a dependency provider for json (org.json:json)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getJson() {
            return create("json");
    }

        /**
         * Creates a dependency provider for postgresql (org.postgresql:postgresql)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getPostgresql() {
            return create("postgresql");
    }

    /**
     * Returns the group of libraries at dagger
     */
    public DaggerLibraryAccessors getDagger() {
        return laccForDaggerLibraryAccessors;
    }

    /**
     * Returns the group of libraries at exposed
     */
    public ExposedLibraryAccessors getExposed() {
        return laccForExposedLibraryAccessors;
    }

    /**
     * Returns the group of libraries at kotlin
     */
    public KotlinLibraryAccessors getKotlin() {
        return laccForKotlinLibraryAccessors;
    }

    /**
     * Returns the group of libraries at logback
     */
    public LogbackLibraryAccessors getLogback() {
        return laccForLogbackLibraryAccessors;
    }

    /**
     * Returns the group of versions at versions
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Returns the group of bundles at bundles
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Returns the group of plugins at plugins
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class DaggerLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {

        public DaggerLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for dagger (com.google.dagger:dagger)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> asProvider() {
                return create("dagger");
        }

            /**
             * Creates a dependency provider for compiler (com.google.dagger:dagger-compiler)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCompiler() {
                return create("dagger.compiler");
        }

    }

    public static class ExposedLibraryAccessors extends SubDependencyFactory {
        private final ExposedKotlinLibraryAccessors laccForExposedKotlinLibraryAccessors = new ExposedKotlinLibraryAccessors(owner);
        private final ExposedSpringLibraryAccessors laccForExposedSpringLibraryAccessors = new ExposedSpringLibraryAccessors(owner);

        public ExposedLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for core (org.jetbrains.exposed:exposed-core)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCore() {
                return create("exposed.core");
        }

            /**
             * Creates a dependency provider for crypt (org.jetbrains.exposed:exposed-crypt)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getCrypt() {
                return create("exposed.crypt");
        }

            /**
             * Creates a dependency provider for dao (org.jetbrains.exposed:exposed-dao)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getDao() {
                return create("exposed.dao");
        }

            /**
             * Creates a dependency provider for jdbc (org.jetbrains.exposed:exposed-jdbc)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJdbc() {
                return create("exposed.jdbc");
        }

            /**
             * Creates a dependency provider for json (org.jetbrains.exposed:exposed-json)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJson() {
                return create("exposed.json");
        }

            /**
             * Creates a dependency provider for money (org.jetbrains.exposed:exposed-money)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getMoney() {
                return create("exposed.money");
        }

        /**
         * Returns the group of libraries at exposed.kotlin
         */
        public ExposedKotlinLibraryAccessors getKotlin() {
            return laccForExposedKotlinLibraryAccessors;
        }

        /**
         * Returns the group of libraries at exposed.spring
         */
        public ExposedSpringLibraryAccessors getSpring() {
            return laccForExposedSpringLibraryAccessors;
        }

    }

    public static class ExposedKotlinLibraryAccessors extends SubDependencyFactory {

        public ExposedKotlinLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for datetime (org.jetbrains.exposed:exposed-kotlin-datetime)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getDatetime() {
                return create("exposed.kotlin.datetime");
        }

    }

    public static class ExposedSpringLibraryAccessors extends SubDependencyFactory {
        private final ExposedSpringBootLibraryAccessors laccForExposedSpringBootLibraryAccessors = new ExposedSpringBootLibraryAccessors(owner);

        public ExposedSpringLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at exposed.spring.boot
         */
        public ExposedSpringBootLibraryAccessors getBoot() {
            return laccForExposedSpringBootLibraryAccessors;
        }

    }

    public static class ExposedSpringBootLibraryAccessors extends SubDependencyFactory {

        public ExposedSpringBootLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for starter (org.jetbrains.exposed:exposed-spring-boot-starter)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getStarter() {
                return create("exposed.spring.boot.starter");
        }

    }

    public static class KotlinLibraryAccessors extends SubDependencyFactory {
        private final KotlinTestLibraryAccessors laccForKotlinTestLibraryAccessors = new KotlinTestLibraryAccessors(owner);

        public KotlinLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

        /**
         * Returns the group of libraries at kotlin.test
         */
        public KotlinTestLibraryAccessors getTest() {
            return laccForKotlinTestLibraryAccessors;
        }

    }

    public static class KotlinTestLibraryAccessors extends SubDependencyFactory {

        public KotlinTestLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for junit (org.jetbrains.kotlin:kotlin-test-junit)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getJunit() {
                return create("kotlin.test.junit");
        }

    }

    public static class LogbackLibraryAccessors extends SubDependencyFactory {

        public LogbackLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for classic (ch.qos.logback:logback-classic)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getClassic() {
                return create("logback.classic");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: dagger (2.52)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getDagger() { return getVersion("dagger"); }

            /**
             * Returns the version associated to this alias: exposed (0.56.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getExposed() { return getVersion("exposed"); }

            /**
             * Returns the version associated to this alias: kotlin (2.0.21)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getKotlin() { return getVersion("kotlin"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

    }

    public static class PluginAccessors extends PluginFactory {

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for ktor to the plugin id 'io.ktor.plugin'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getKtor() { return createPlugin("ktor"); }

            /**
             * Creates a plugin provider for serialization to the plugin id 'org.jetbrains.kotlin.plugin.serialization'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getSerialization() { return createPlugin("serialization"); }

    }

}
