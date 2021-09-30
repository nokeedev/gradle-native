/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		return (T) ModelNodes.of(this).getDescendant("sources").get(ComponentSources.class);
	}

	/**
	 * Configures the component sources using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 */
	default void sources(Action<? super T> action) {
		ModelNodes.of(this).getDescendant("sources")
			.applyTo(self(stateAtLeast(ModelNode.State.Realized)).apply(node -> action.execute((T) node.get(ComponentSources.class))));
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
