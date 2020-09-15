package dev.nokee.platform.nativebase;

import dev.nokee.platform.base.ApplicationComponentDependencies;
import dev.nokee.platform.base.ComponentDependencies;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.util.ConfigureUtil;

/**
 * Allows the implementation dependencies of a native application to be specified.
 *
 * @since 0.5
 */
public interface NativeApplicationComponentDependencies extends ApplicationComponentDependencies, NativeComponentDependencies, ComponentDependencies {
	/**
	 * {@inheritDoc}
	 */
	@Override
	default void implementation(Object notation, @DelegatesTo(value = ModuleDependency.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		implementation(notation, ConfigureUtil.configureUsing(closure));
	}
}
