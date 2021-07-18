package dev.nokee.platform.cpp;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component sources that carries an C++ source set named {@literal cpp}.
 *
 * @see ComponentSources
 * @see CppSourceSet
 * @since 0.5
 */
public interface HasCppSourceSet {
	/**
	 * Returns a C++ source set provider for the source set named {@literal cpp}.
	 *
	 * @return a provider for {@literal cpp} source set, never null
	 */
	default DomainObjectProvider<CppSourceSet> getCpp() {
		return ((FunctionalSourceSet) this).get("cpp", CppSourceSet.class);
	}

	/**
	 * Configures the C++ source set named {@literal cpp} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getCpp()
	 */
	default void cpp(Action<? super CppSourceSet> action) {
		getCpp().configure(action);
	}

	/**
	 * Configures the C++ source set named {@literal cpp} using the specified configuration closure.
	 *
	 * @param closure  the configuration action, must not be null
	 * @see #getCpp()
	 */
	default void cpp(@DelegatesTo(value = CppSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		cpp(configureUsing(closure));
	}
}
