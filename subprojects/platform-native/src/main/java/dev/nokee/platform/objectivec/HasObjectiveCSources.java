package dev.nokee.platform.objectivec;

import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * Represents a component that carries Objective-C sources.
 *
 * @see HasObjectiveCSourceSet
 * @since 0.5
 */
public interface HasObjectiveCSources {
	/**
	 * Defines the Objective-C sources of this component.
	 *
	 * <p>By default, the source set contains all files in the directory {@code src/componentName/objectiveC} (and {@code src/componentName/objc} for backward compatibility), where {@literal componentName} represent this component's name, i.e. {@literal main} or {@literal test}.
	 *
	 * @return a source set containing the Objective-C sources of this component, never null
	 * @see ObjectiveCSourceSet
	 * @since 0.5
	 */
	default ObjectiveCSourceSet getObjectiveCSources() {
		return ((HasObjectiveCSourceSet) sourceViewOf(this)).getObjectiveC().get();
	}

	/**
	 * Configures the Objective-C sources of this component using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getObjectiveCSources()
	 */
	default void objectiveCSources(Action<? super ObjectiveCSourceSet> action) {
		((HasObjectiveCSourceSet) sourceViewOf(this)).getObjectiveC().configure(action);
	}

	/**
	 * Configures the Objective-C sources of this component using the specified configuration closure.
	 *
	 * @param closure  the configuration closure, must not be null
	 * @see #getObjectiveCSources()
	 */
	default void objectiveCSources(@DelegatesTo(value = ObjectiveCSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		objectiveCSources(configureUsing(closure));
	}
}
