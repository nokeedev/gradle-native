package dev.nokee.platform.cpp;

import dev.nokee.language.cpp.CppSourceSet;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component that carries C++ sources.
 *
 * @see HasCppSourceSet
 * @since 0.5
 */
public interface HasCppSources {
	/**
	 * Defines the C++ sources of this component.
	 *
	 * <p>By default, the source set contains all files in the directory {@code src/componentName/cpp}, where {@literal componentName} represent this component's name, i.e. {@literal main} or {@literal test}.
	 *
	 * @return a source set containing the C++ sources of this component, never null
	 * @see CppSourceSet
	 */
	default CppSourceSet getCppSources() {
		return ((HasCppSourceSet) sourceViewOf(this)).getCpp().get();
	}

	/**
	 * Configures the C++ sources of this component using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getCppSources()
	 */
	default void cppSources(Action<? super CppSourceSet> action) {
		((HasCppSourceSet) sourceViewOf(this)).getCpp().configure(action);
	}

	/**
	 * Configures the C++ sources of this component using the specified configuration closure.
	 *
	 * @param closure  the configuration closure, must not be null
	 * @see #getCppSources()
	 */
	default void cppSources(@DelegatesTo(value = CppSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		cppSources(configureUsing(closure));
	}
}
