package dev.nokee.platform.base;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

/**
 * A component with dependency buckets.
 *
 * @param <T> type of the component dependencies
 * @since 0.4
 */
public interface DependencyAwareComponent<T extends ComponentDependencies> {
	/**
	 * Returns the dependencies of this component.
	 *
	 * @return a {@link ComponentDependencies}, never null.
	 */
	T getDependencies();

	/**
	 * Configure the dependencies of this component.
	 *
	 * @param action configuration action for {@link ComponentDependencies}.
	 */
	default void dependencies(Action<? super T> action) {
		action.execute(getDependencies());
	}

	/**
	 * Configure the dependencies of this component.
	 *
	 * @param closure configuration closure for {@link ComponentDependencies}.
	 */
	default void dependencies(@DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		dependencies(ConfigureUtil.configureUsing(closure));
	}
}

