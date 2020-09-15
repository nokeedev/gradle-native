package dev.nokee.platform.nativebase;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.LibraryComponentDependencies;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.util.ConfigureUtil;

/**
 * Allows the API and implementation dependencies of a native library to be specified.
 *
 * @since 0.5
 */
public interface NativeLibraryComponentDependencies extends LibraryComponentDependencies, NativeComponentDependencies, ComponentDependencies, NativeLibraryDependencies {
	/**
	 * {@inheritDoc}
	 */
	@Override
	default void api(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		api(notation, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default void implementation(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		implementation(notation, ConfigureUtil.configureUsing(closure));
	}
}
