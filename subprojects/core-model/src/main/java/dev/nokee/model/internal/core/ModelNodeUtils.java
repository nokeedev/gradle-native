/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.model.internal.core;

import java.util.Optional;

// TODO: Remove "maybe add" custom logic to favour dedup within ModelProjection adding logic
public final class ModelNodeUtils {

	private ModelNodeUtils() {}

	/**
	 * Returns the parent node of the specified node, if available.
	 *
	 * @param self  the node to query parent, must not be null
	 * @return the parent model node, never null but can be absent
	 */
	public static Optional<ModelNode> getParent(ModelNode self) {
		return self.find(ParentComponent.class).map(ParentComponent::get);
	}

	/**
	 * Returns the path of this model node.
	 *
	 * @param self  the entity to return the path
	 * @return a {@link ModelPath} representing this model node, never null.
	 */
	public static ModelPath getPath(ModelNode self) {
		return self.get(ModelPathComponent.class).get();
	}
}
