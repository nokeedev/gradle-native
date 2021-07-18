package dev.nokee.platform.objectivecpp;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component sources that carries an Objective-C++ source set named {@literal objectiveCpp}.
 *
 * @see ComponentSources
 * @see ObjectiveCppSourceSet
 * @since 0.5
 */
public interface HasObjectiveCppSourceSet {
	/**
	 * Returns a Objective-C++ source set provider for the source set named {@literal objectiveCpp}.
	 *
	 * @return a provider for {@literal objectiveCpp} source set, never null
	 */
	default DomainObjectProvider<ObjectiveCppSourceSet> getObjectiveCpp() {
		return ((FunctionalSourceSet) this).get("objectiveCpp", ObjectiveCppSourceSet.class);
	}

	/**
	 * Configures the Objective-C++ source set named {@literal objectiveCpp} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getObjectiveCpp()
	 */
	default void objectiveCpp(Action<? super ObjectiveCppSourceSet> action) {
		getObjectiveCpp().configure(action);
	}

	/**
	 * Configures the Objective-C++ source set named {@literal objectiveCpp} using the specified configuration closure.
	 *
	 * @param closure  the configuration action, must not be null
	 * @see #getObjectiveCpp()
	 */
	default void objectiveCpp(@DelegatesTo(value = ObjectiveCppSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		objectiveCpp(configureUsing(closure));
	}
}
