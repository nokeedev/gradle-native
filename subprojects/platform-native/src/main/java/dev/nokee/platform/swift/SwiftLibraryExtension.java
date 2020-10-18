package dev.nokee.platform.swift;

import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.*;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

/**
 * Configuration for a library written in Swift, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Swift Library Plugin.</p>
 *
 * @since 0.4
 */
public interface SwiftLibraryExtension extends DependencyAwareComponent<NativeLibraryComponentDependencies>, VariantAwareComponent<NativeLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, TargetLinkageAwareComponent, TargetBuildTypeAwareComponent {
	/**
	 * Defines the source files or directories of this library.
	 * You can add files or directories to this collection.
	 * When a directory is added, all source files are included for compilation.
	 *
	 * <p>When this collection is empty, the directory {@code src/main/swift} is used by default.</p>
	 *
	 * @since 0.5
	 */
	SwiftSourceSet getSwiftSources();

	/**
	 * Configures the source files or directories of this library.
	 *
	 * @param action The action to execute for source set configuration.
	 * @see #getSwiftSources()
	 */
	void swiftSources(Action<? super SwiftSourceSet> action);

	/**
	 * Configures the source files or directories of this library.
	 *
	 * @param closure The closure to execute for source set configuration.
	 * @see #getSwiftSources()
	 */
	default void swiftSources(@DelegatesTo(value = SwiftSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		swiftSources(ConfigureUtil.configureUsing(closure));
	}
}
