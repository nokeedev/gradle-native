package dev.nokee.platform.ios;

import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

public interface SwiftIosApplicationExtension extends DependencyAwareComponent<NativeComponentDependencies>, VariantAwareComponent<IosApplication>, BinaryAwareComponent, SourceAwareComponent {
	/**
	 * Defines the source files or directories of this application.
	 * You can add files or directories to this collection.
	 * When a directory is added, all source files are included for compilation.
	 *
	 * <p>When this collection is empty, the directory {@code src/main/swift} is used by default.</p>
	 *
	 * @since 0.5
	 */
	SwiftSourceSet getSwiftSources();

	/**
	 * Configures the source files or directories of this application.
	 *
	 * @param action The action to execute for source set configuration.
	 * @see #getSwiftSources()
	 */
	void swiftSources(Action<? super SwiftSourceSet> action);

	/**
	 * Configures the source files or directories of this application.
	 *
	 * @param closure The closure to execute for source set configuration.
	 * @see #getSwiftSources()
	 */
	default void swiftSources(@DelegatesTo(value = SwiftSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		swiftSources(ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Defines the iOS resources directories of this application.
	 *
	 * <p>When this collection is empty, the directory {@code src/main/resources} is used by default.</p>
	 *
	 * @since 0.5
	 */
	IosResourceSet getResources();

	/**
	 * Configures the resources directories of this application.
	 *
	 * @param action The action to execute for source set configuration.
	 * @see #getResources()
	 * @since 0.5
	 */
	void resources(Action<? super IosResourceSet> action);

	/**
	 * Configures the iOS resources directories of this application.
	 *
	 * @param closure The action to execute for source set configuration.
	 * @see #getResources()
	 * @since 0.5
	 */
	default void resources(@DelegatesTo(value = IosResourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		resources(ConfigureUtil.configureUsing(closure));
	}
}
