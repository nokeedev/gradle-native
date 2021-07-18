package dev.nokee.platform.objectivecpp;

import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;

/**
 * Represents a component that carries Objective-C++ sources.
 *
 * @see HasObjectiveCppSourceSet
 * @since 0.5
 */
public interface HasObjectiveCppSources {
	/**
	 * Defines the Objective-C++ sources of this component.
	 *
	 * <p>By default, the source set contains all files in the directory {@code src/componentName/objectiveCpp} (and {@code src/componentName/objcpp} for backward compatibility), where {@literal componentName} represent this component's name, i.e. {@literal main} or {@literal test}.
	 *
	 * @return a source set containing the Objective-C++ sources of this component, never null
	 * @see ObjectiveCppSourceSet
	 */
	default ObjectiveCppSourceSet getObjectiveCppSources() {
		return ((HasObjectiveCppSourceSet) sourceViewOf(this)).getObjectiveCpp().get();
	}

	/**
	 * Configures the Objective-C++ sources of this component using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @see #getObjectiveCppSources()
	 */
	default void objectiveCppSources(Action<? super ObjectiveCppSourceSet> action) {
		((HasObjectiveCppSourceSet) sourceViewOf(this)).getObjectiveCpp().configure(action);
	}

	/**
	 * Configures the Objective-C++ sources of this component using the specified configuration closure.
	 *
	 * @param closure  the configuration closure, must not be null
	 * @see #getObjectiveCppSources()
	 */
	default void objectiveCppSources(@DelegatesTo(value = ObjectiveCppSourceSet.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		objectiveCppSources(ConfigureUtil.configureUsing(closure));
	}
}
