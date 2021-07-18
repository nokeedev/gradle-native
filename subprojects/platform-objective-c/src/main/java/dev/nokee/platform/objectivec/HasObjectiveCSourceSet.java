package dev.nokee.platform.objectivec;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component sources that carries an Objective-C source set named {@literal objectiveC}.
 *
 * @see ComponentSources
 * @see ObjectiveCSourceSet
 * @since 0.5
 */
public interface HasObjectiveCSourceSet {
	/**
	 * Returns a Objective-C source set provider for the source set named {@literal objectiveC}.
	 *
	 * @return a provider for {@literal objectiveC} source set, never null
	 */
	default DomainObjectProvider<ObjectiveCSourceSet> getObjectiveC() {
		return ((FunctionalSourceSet) this).get("objectiveC", ObjectiveCSourceSet.class);
	}

	/**
	 * Configures the Objective-C source set named {@literal objectiveC} using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getObjectiveC()
	 */
	default void objectiveC(Action<? super ObjectiveCSourceSet> action) {
		getObjectiveC().configure(action);
	}

	/**
	 * Configures the Objective-C source set named {@literal objectiveC} using the specified configuration closure.
	 *
	 * @param closure  the configuration action, must not be null
	 * @see #getObjectiveC()
	 */
	default void objectiveC(@DelegatesTo(value = ObjectiveCSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		objectiveC(configureUsing(closure));
	}
}
