package dev.nokee.platform.base;

import org.gradle.api.Action;

public interface DependencyAwareComponent<T extends ComponentDependencies> {
	/**
	 * Returns the dependencies of this component.
	 *
	 * @return a {@link ComponentDependencies}, never null.
	 * @since 0.1
	 */
	T getDependencies();

	/**
	 * Configure the dependencies of this component.
	 *
	 * @param action configuration action for {@link ComponentDependencies}.
	 * @since 0.1
	 */
	void dependencies(Action<? super T> action);
}
