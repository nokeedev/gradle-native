package dev.nokee.platform.nativebase;

import dev.nokee.platform.base.ComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ExternalModuleDependency;

/**
 * Allows the implementation dependencies of a native component to be specified.
 *
 * @since 0.4
 */
public interface NativeComponentDependencies extends ComponentDependencies {
    /**
     * Adds an implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled against this component.
     *
     * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
     */
    void implementation(Object notation);

    /**
     * Adds an implementation dependency to this component.
	 * An implementation dependency is not visible to consumers that are compiled against this component.
     *
     * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
     * @param action The action to run to configure the dependency.
     */
    void implementation(Object notation, Action<? super ExternalModuleDependency> action);

	/**
	 * Adds an native compile only dependency to this component.
	 * An compile only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void compileOnly(Object notation);

	/**
	 * Adds an native compile only dependency to this component.
	 * An compile only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency.
	 */
	void compileOnly(Object notation, Action<? super ExternalModuleDependency> action);

	/**
	 * Adds an native link only dependency to this component.
	 * An link only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void linkOnly(Object notation);

	/**
	 * Adds an native link only dependency to this component.
	 * An link only dependency is not visible to consumers that are compiled or linked against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency.
	 */
	void linkOnly(Object notation, Action<? super ExternalModuleDependency> action);

	/**
	 * Adds an native runtime only dependency to this component.
	 * An runtime only dependency is not visible to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 */
	void runtimeOnly(Object notation);

	/**
	 * Adds an native runtime only dependency to this component.
	 * An runtime only dependency is visible only to consumers that are running against this component.
	 *
	 * @param notation The dependency notation, as per {@link org.gradle.api.artifacts.dsl.DependencyHandler#create(Object)}.
	 * @param action The action to run to configure the dependency.
	 */
	void runtimeOnly(Object notation, Action<? super ExternalModuleDependency> action);
}
