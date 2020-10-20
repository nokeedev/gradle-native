package dev.nokee.platform.objectivecpp;

import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.*;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

/**
 * Configuration for a library written in Objective-C++, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Objective-C++ Library Plugin.</p>
 *
 * @since 0.4
 */
public interface ObjectiveCppLibraryExtension extends DependencyAwareComponent<NativeLibraryComponentDependencies>, VariantAwareComponent<NativeLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, TargetLinkageAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent {
	/**
	 * Defines the source files or directories of this library.
	 * You can add files or directories to this collection.
	 * When a directory is added, all source files are included for compilation.
	 *
	 * <p>When this collection is empty, the directory {@code src/main/objcpp} is used by default.</p>
	 *
	 * @since 0.5
	 */
	ObjectiveCppSourceSet getObjectiveCppSources();

	/**
	 * Configures the source files or directories of this application.
	 *
	 * @param action The action to execute for source set configuration.
	 * @see #getObjectiveCppSources()
	 * @since 0.5
	 */
	void objectiveCppSources(Action<? super ObjectiveCppSourceSet> action);

	/**
	 * Configures the source files or directories of this application.
	 *
	 * @param closure The closure to execute for source set configuration.
	 * @see #getObjectiveCppSources()
	 * @since 0.5
	 */
	default void objectiveCppSources(@DelegatesTo(value = ObjectiveCppSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		objectiveCppSources(ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Defines the private headers search directories of this library.
	 *
	 * <p>When this collection is empty, the directory {@code src/main/headers} is used by default.</p>
	 *
	 * @since 0.5
	 */
	CppHeaderSet getPrivateHeaders();

	/**
	 * Configures the private headers search directories of this library.
	 *
	 * @param action The action to execute for source set configuration.
	 * @see #getPrivateHeaders()
	 * @since 0.5
	 */
	void privateHeaders(Action<? super CppHeaderSet> action);

	/**
	 * Configures the private headers search directories of this library.
	 *
	 * @param closure The action to execute for source set configuration.
	 * @see #getPrivateHeaders()
	 * @since 0.5
	 */
	default void privateHeaders(@DelegatesTo(value = CppHeaderSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		privateHeaders(ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Defines the public header file directories of this library.
	 *
	 * <p>When this collection is empty, the directory {@code src/main/public} is used by default.</p>
	 *
	 * @since 0.5
	 */
	CppHeaderSet getPublicHeaders();

	/**
	 * Configures the public headers search directories of this library.
	 *
	 * @param action The action to execute for source set configuration.
	 * @see #getPublicHeaders()
	 * @since 0.5
	 */
	void publicHeaders(Action<? super CppHeaderSet> action);

	/**
	 * Configures the public headers search directories of this library.
	 *
	 * @param closure The action to execute for source set configuration.
	 * @see #getPublicHeaders()
	 * @since 0.5
	 */
	default void publicHeaders(@DelegatesTo(value = CppHeaderSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		publicHeaders(ConfigureUtil.configureUsing(closure));
	}
}
