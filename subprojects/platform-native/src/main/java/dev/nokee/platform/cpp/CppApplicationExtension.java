package dev.nokee.platform.cpp;

import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

/**
 * Configuration for an application written in C++, defining the dependencies that make up the application plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the C++ Application Plugin.</p>
 *
 * @since 0.4
 */
public interface CppApplicationExtension extends DependencyAwareComponent<NativeApplicationComponentDependencies>, VariantAwareComponent<NativeApplication>, BinaryAwareComponent, TargetMachineAwareComponent, TargetBuildTypeAwareComponent {
	/**
	 * Defines the source files or directories of this application.
	 * You can add files or directories to this collection.
	 * When a directory is added, all source files are included for compilation.
	 *
	 * <p>When this collection is empty, the directory {@code src/main/cpp} is used by default.</p>
	 *
	 * @since 0.5
	 */
	CppSourceSet getCppSources();

	/**
	 * Configures the source files or directories of this application.
	 *
	 * @param action The action to execute for source set configuration.
	 * @see #getCppSources()
	 * @since 0.5
	 */
	void cppSources(Action<? super CppSourceSet> action);

	/**
	 * Configures the source files or directories of this application.
	 *
	 * @param closure The closure to execute for source set configuration.
	 * @see #getCppSources()
	 * @since 0.5
	 */
	default void cppSources(@DelegatesTo(value = CppSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		cppSources(ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Defines the private headers search directories of this application.
	 *
	 * <p>When this collection is empty, the directory {@code src/main/headers} is used by default.</p>
	 *
	 * @since 0.5
	 */
	CppHeaderSet getPrivateHeaders();

	/**
	 * Configures the private headers search directories of this application.
	 *
	 * @param action The action to execute for source set configuration.
	 * @see #getPrivateHeaders()
	 * @since 0.5
	 */
	void privateHeaders(Action<? super CppHeaderSet> action);

	/**
	 * Configures the private headers search directories of this application.
	 *
	 * @param closure The action to execute for source set configuration.
	 * @see #getPrivateHeaders()
	 * @since 0.5
	 */
	default void privateHeaders(@DelegatesTo(value = CppHeaderSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		privateHeaders(ConfigureUtil.configureUsing(closure));
	}
}
