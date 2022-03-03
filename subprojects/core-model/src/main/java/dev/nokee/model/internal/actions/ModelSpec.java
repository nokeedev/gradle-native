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

import dev.nokee.model.internal.ElementName;
import dev.nokee.model.internal.FullyQualifiedName;
import dev.nokee.model.internal.core.ModelEntityId;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;

import java.util.Objects;

/**
 * A model specification represent a smart query to match entities scoped by the {@link ModelAction}.
 *
 * <b>Note:</b> This interface is not intended for implementation by build script or plugin authors.
 * Use one of the factory methods available.
 */
public interface ModelSpec {
	/**
	 * Creates a new intersecting specification where both this and the specified specification needs to be satisfied.
	 *
	 * @param other  the other specification to satisfy, must not be null
	 * @return a new intersecting specification, never null
	 */
	default ModelSpec and(ModelSpec other) {
		return new AndSpec(this, other);
	}

	/**
	 * Creates a specification that match for the specified fully qualified name.
	 *
	 * @param name  the entity name to satisfy, must not be null
	 * @return a new specification matching the specified name, never null
	 * @see FullyQualifiedName
	 */
	static ModelSpec named(FullyQualifiedName name) {
		return new IdentitySpec(Objects.requireNonNull(name));
	}

	/**
	 * Creates a specification that match for the specified element name.
	 *
	 * @param name  the entity name to satisfy, must not be null
	 * @return a new specification matching the specified name, never null
	 * @see ElementName
	 */
	static ModelSpec named(ElementName name) {
		return new IdentitySpec(Objects.requireNonNull(name));
	}

	/**
	 * Creates a specification that match for the specified base type.
	 *
	 * @param type  a base type to match entity projections, must not be null
	 * @return a new specification matching the specified base type, never null
	 */
	static ModelSpec subtypeOf(ModelType<?> type) {
		return new WithTypeSpec(Objects.requireNonNull(type));
	}

	/**
	 * Creates a specification that match at least the specified state.
	 *
	 * @param state  the minimum entity state to satisfy, must not be null
	 * @return a new specification matching the specified entity state, never null
	 */
	static ModelSpec stateAtLeast(ModelState state) {
		return new StateAtLeastSpec(Objects.requireNonNull(state));
	}

	/**
	 * Creates a specification that match the direct parent of an entity.
	 *
	 * @param entityRef  the parent entity reference to satisfy, must not be null
	 * @return a new specification matching the specified parent entity, never null
	 */
	static ModelSpec ownedBy(ModelEntityId entityRef) {
		return new IdentitySpec(new ParentRef(Objects.requireNonNull(entityRef)));
	}

	/**
	 * Creates a specification that match any ancestors of an entity.
	 *
	 * @param entityRef  a ancestor entity reference to satisfy, must not be null
	 * @return a new specification matching the specified ancestor entity, never null
	 */
	static ModelSpec descendantOf(ModelEntityId entityRef) {
		return new AncestorSpec(Objects.requireNonNull(entityRef));
	}
}
