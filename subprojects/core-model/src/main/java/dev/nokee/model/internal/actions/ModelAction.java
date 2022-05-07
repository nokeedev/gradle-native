/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.model.internal.actions;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.ModelEntityId;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.FullyQualifiedName;
import dev.nokee.model.internal.state.ModelState;
import org.gradle.api.Action;

import static dev.nokee.model.internal.actions.ModelSpec.isEqual;
import static dev.nokee.model.internal.actions.ModelSpec.self;
import static dev.nokee.model.internal.actions.ModelSpec.stateAtLeast;
import static dev.nokee.model.internal.actions.ModelSpec.subtypeOf;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;

/**
 * Represent an action to execute against a projection of the entity.
 */
public interface ModelAction {
	/**
	 * Performs this action against given entity.
	 *
	 * @param entity  the entity to perform the action on, never null
	 */
	void execute(ModelNode entity);

	/**
	 * Creates action registration that configures each entity using the projection of the specified type.
	 * The model action assume a state of at least {@link ModelState#Realized}.
	 *
	 * @param type  the projection type to perform the action on, must not be null
	 * @param action  the action to perform, must not be null
	 * @param <T>  the projection type
	 * @return a registration for model action, never null
	 */
	static <T> ModelRegistration configureEach(Class<T> type, Action<? super T> action) {
		return configureMatching(stateAtLeast(ModelState.Realized).and(subtypeOf(of(type))),
			new ModelActionAdapter<>(of(type), action));
	}

	/**
	 * Creates action registration that configures each entity matching the specified spec using the projection of the specified type.
	 * The model action assume a state of at least {@link ModelState#Realized}.
	 *
	 * @param spec  a specification to satisfied, must not be null
	 * @param type  the projection type to perform the action on, must not be null
	 * @param action  the action to perform, must not be null
	 * @param <T>  the projection type
	 * @return a registration for model action, never null
	 */
	static <T> ModelRegistration configureEach(ModelSpec spec, Class<T> type, Action<? super T> action) {
		return configureMatching(stateAtLeast(ModelState.Realized).and(subtypeOf(of(type))).and(spec),
			new ModelActionAdapter<>(of(type), action));
	}

	/**
	 * Creates action registration that configures each entity when the projection of the specified type is known.
	 * The model action assume a state of at least {@link ModelState#Registered}.
	 *
	 * @param type  the projection type to perform the action when known, must not be null
	 * @param action  the action to perform, must not be null
	 * @param <T>  the projection type
	 * @return a registration for model action, never null
	 */
	static <T> ModelRegistration whenElementKnown(Class<T> type, Action<? super KnownDomainObject<T>> action) {
		return configureMatching(stateAtLeast(ModelState.Registered).and(subtypeOf(of(type))),
			new ModelActionKnownDomainObjectAdapter<>(of(type), action));
	}

	/**
	 * Creates action registration that configures each entity matching the specified spec and when the projection of the specified type is known.
	 * The model action assume a state of at least {@link ModelState#Registered}.
	 *
	 * @param type  the projection type to perform the action when known, must not be null
	 * @param action  the action to perform, must not be null
	 * @param <T>  the projection type
	 * @return a registration for model action, never null
	 */
	static <T> ModelRegistration whenElementKnown(ModelSpec spec, Class<T> type, Action<? super KnownDomainObject<T>> action) {
		return configureMatching(stateAtLeast(ModelState.Registered).and(subtypeOf(of(type))).and(spec),
			new ModelActionKnownDomainObjectAdapter<>(of(type), action));
	}

	/**
	 * Creates a registration to configure the specified fully qualified domain object with the specified projection type.
	 * The model action assume a state of at least {@link ModelState#Realized}.
	 *
	 * @param name  the fully qualified name of the domain object to configure, must not be null
	 * @param type  the projection type to perform the action, must not be null
	 * @param action  the action to perform, must not be null
	 * @param <T>  the projection type
	 * @return a registration for model action, never null
	 */
	static <T> ModelRegistration configure(String name, Class<T> type, Action<? super T> action) {
		return configureMatching(stateAtLeast(ModelState.Realized).and(isEqual(FullyQualifiedName.of(name))).and(subtypeOf(of(type))),
			new ModelActionAdapter<>(of(type), action));
	}

	/**
	 * Creates a registration to configure a specific domain object with the specified projection type.
	 * The model action assume a state of at least {@link ModelState#Realized}.
	 *
	 * @param id  the entity identifier, must not be null
	 * @param type  the projection type to perform the action, must not be null
	 * @param action  the action to perform, must not be null
	 * @param <T>  the projection type
	 * @return a registration for model action, never null
	 */
	static <T> ModelRegistration configure(ModelEntityId id, Class<T> type, Action<? super T> action) {
		return configureMatching(self(id).and(stateAtLeast(ModelState.Realized)).and(subtypeOf(of(type))),
			new ModelActionAdapter<>(of(type), action));
	}

	/**
	 * Creates a registration to configure the entity matching the specified spec using the specified action.
	 *
	 * @param spec  the spec to satisfy, must not be null
	 * @param action  the action to perform, must not be null
	 * @return a registration for model action, never null
	 */
	static ModelRegistration configureMatching(ModelSpec spec, ModelAction action) {
		return ModelRegistration.builder()
			.withComponent(new ModelSpecComponent(spec))
			.withComponent(new ModelActionComponent(action))
			.withComponent(tag(ModelActionTag.class))
			.build();
	}
}
