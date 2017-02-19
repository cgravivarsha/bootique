package io.bootique.test.junit;

import com.google.inject.Module;
import io.bootique.BQCoreModule;
import io.bootique.BQModuleOverrideBuilder;
import io.bootique.BQModuleProvider;
import io.bootique.Bootique;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @since 0.20
 */
// parameterization is needed to enable covariant return types in subclasses
public abstract class BQTestRuntimeBuilder<T extends BQTestRuntimeBuilder<T>> {

    protected Bootique bootique;
    protected Map<String, String> properties;

    protected BQTestRuntimeBuilder(String[] args) {
        this.properties = new HashMap<>();
        this.bootique = Bootique.app(args).module(createPropertiesProvider());
    }

    protected BQModuleProvider createPropertiesProvider() {
        return new BQModuleProvider() {

            @Override
            public Module module() {
                return binder -> BQCoreModule.contribute(binder).setProperties(properties);
            }

            @Override
            public String name() {
                return "BQTestRuntimeBuilder:properties";
            }
        };
    }

    /**
     * Appends extra values to the test CLI arguments.
     *
     * @param args extra args to pass to Bootique.
     * @return this instance of test runtime builder.
     */
    public T args(String... args) {
        bootique.args(args);
        return (T) this;
    }

    /**
     * Appends extra values to the test CLI arguments.
     *
     * @param args extra args to pass to Bootique.
     * @return this instance of test runtime builder.
     */
    public T args(Collection<String> args) {
        bootique.args(args);
        return (T) this;
    }

    /**
     * Instructs Bootique to load any modules available on class-path that
     * expose {@link io.bootique.BQModuleProvider} provider. Auto-loaded modules will be
     * used in default configuration. Factories within modules will of course be
     * configured dynamically from YAML.
     *
     * @return this instance of test runtime builder.
     */
    public T autoLoadModules() {
        bootique.autoLoadModules();
        return (T) this;
    }

    /**
     * @param moduleType custom Module class to add to Bootique DI runtime.
     * @return this instance of test runtime builder.
     * @see #autoLoadModules()
     */
    public T module(Class<? extends Module> moduleType) {
        bootique.module(moduleType);
        return (T) this;
    }

    /**
     * Adds an array of Module types to the Bootique DI runtime. Each type will
     * be instantiated by Bootique and added to the Guice DI container.
     *
     * @param moduleTypes custom Module classes to add to Bootique DI runtime.
     * @return this instance of test runtime builder.
     * @see #autoLoadModules()
     */
    public T modules(Class<? extends Module>... moduleTypes) {
        bootique.modules(moduleTypes);
        return (T) this;
    }

    /**
     * @param m a module to add to the test runtime.
     * @return this instance of test runtime builder.
     */
    public T module(Module m) {
        bootique.module(m);
        return (T) this;
    }

    /**
     * Adds an array of Modules to the Bootique DI runtime.
     *
     * @param modules an array of modules to add to Bootiqie DI runtime.
     * @return this instance of test runtime builder.
     */
    public T modules(Module... modules) {
        bootique.modules(modules);
        return (T) this;
    }

    /**
     * Adds a Module generated by the provider. Provider may optionally specify
     * that the Module overrides services in some other Module.
     *
     * @param moduleProvider a provider of Module and override spec.
     * @return this instance of test runtime builder.
     */
    public T module(BQModuleProvider moduleProvider) {
        bootique.module(moduleProvider);
        return (T) this;
    }

    /**
     * Starts an API call chain to override an array of Modules.
     *
     * @param overriddenTypes an array of modules whose bindings should be overridden.
     * @return {@link BQModuleOverrideBuilder} object to specify a Module
     * overriding other modules.
     */
    public BQModuleOverrideBuilder<T> override(Class<? extends Module>... overriddenTypes) {

        BQModuleOverrideBuilder<Bootique> subBuilder = bootique.override(overriddenTypes);
        return new BQModuleOverrideBuilder<T>() {

            @Override
            public T with(Class<? extends Module> moduleType) {
                subBuilder.with(moduleType);
                return (T) BQTestRuntimeBuilder.this;
            }

            @Override
            public T with(Module module) {
                subBuilder.with(module);
                return (T) BQTestRuntimeBuilder.this;
            }
        };
    }

    public T property(String key, String value) {
        properties.put(key, value);
        return (T) this;
    }

    /**
     * @param configurator a callback function that configures Bootique stack.
     * @return this builder instance.
     * @deprecated since 0.20 use builder methods directly instead of configurator function to add modules, etc.
     */
    @Deprecated
    public T configurator(Consumer<Bootique> configurator) {
        Objects.requireNonNull(configurator).accept(bootique);
        return (T) this;
    }
}
