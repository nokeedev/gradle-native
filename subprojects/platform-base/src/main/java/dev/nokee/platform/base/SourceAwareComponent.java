package dev.nokee.platform.base;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * A component with sources.
 *
 * @param <T>  the component sources type
 * @since 0.5
 */
public interface SourceAwareComponent<T extends ComponentSources> extends Component {
	/**
	 * Returns the component sources of this component.
	 *
	 * @return the component sources of this component, never null
	 */
	default T getSources() {
		return (T) ModelNodes.of(this).getDescendant("sources").get(Object.class);
	}

	/**
	 * Configures the component sources using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 */
	default void sources(Action<? super T> action) {
		ModelNodes.of(this).getDescendant("sources")
			.applyTo(self(stateAtLeast(ModelNode.State.Realized)).apply(node -> action.execute((T) node.get(Object.class))));
	}

	/**
	 * Configures the component sources using the specified configuration closure.
	 *
	 * @param closure  the configuration closure, must not be null
	 */
	default void sources(@DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		sources(configureUsing(closure));
	}
}
