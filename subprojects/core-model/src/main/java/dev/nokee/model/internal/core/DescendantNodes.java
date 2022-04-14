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

import dev.nokee.model.internal.registry.ModelLookup;

import java.util.List;
import java.util.Optional;

import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;

public final class DescendantNodes implements ModelComponent {
	private final ModelLookup modelLookup;
	private final ModelPath path;

	public DescendantNodes(ModelLookup modelLookup, ModelPath path) {
		this.modelLookup = modelLookup;
		this.path = path;
	}

	/**
	 * Returns the direct descending nodes.
	 *
	 * @return a list of directly descending nodes, never null.
	 */
	public List<ModelNode> getDirectDescendants() {
		return modelLookup.query(allDirectDescendants().scope(path)).get();
	}

	public ModelNode getDescendant(String name) {
		return modelLookup.get(path.child(name));
	}

	public boolean hasDescendant(String name) {
		return modelLookup.has(path.child(name));
	}

	public Optional<ModelNode> findDescendant(String name) {
		return modelLookup.find(path.child(name));
	}
}
