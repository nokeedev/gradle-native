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
package dev.nokee.model.internal.core;

/**
 * A model node state listener.
 */
public interface ModelNodeListener {
	/**
	 * A listener that does nothing on all callback.
	 *
	 * @return a model node listener that perform no operation on callbacks, never null
	 */
	static ModelNodeListener noOpListener() {
		return new ModelNodeListener() {
			@Override
			public void projectionAdded(ModelNode node, ModelComponent newComponent) {
				// do nothing
			}
		};
	}

	/**
	 * When the model node has new projection.
	 *
	 * @param node  the model node that has a new projection
	 * @param newComponent  the new added component to the entity
	 */
	void projectionAdded(ModelNode node, ModelComponent newComponent);
}
