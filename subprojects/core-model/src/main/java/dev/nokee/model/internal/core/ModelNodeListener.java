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

import dev.nokee.model.internal.state.ModelState;

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
			public void created(ModelNode node) {
				// do nothing
			}

			@Override
			public void initialized(ModelNode modelNode) {
				// do nothing
			}

			@Override
			public void registered(ModelNode modelNode) {
				// do nothing
			}

			@Override
			public void realized(ModelNode node) {
				// do nothing
			}

			@Override
			public void projectionAdded(ModelNode node, Object newComponent) {
				// do nothing
			}
		};
	}

	/**
	 * When the model node transition to {@link ModelState#Created}.
	 *
	 * @param node  the model node that transitioned to the created state
	 */
	void created(ModelNode node);

	/**
	 * When the model node transition to {@link ModelState#Initialized}.
	 *
	 * @param node  the model node that transitioned to the initialized state
	 */
	void initialized(ModelNode node);

	/**
	 * When the model node transition to {@link ModelState#Registered}.
	 *
	 * @param node  the model node that transitioned to the registered state
	 */
	void registered(ModelNode node);

	/**
	 * When the model node transition to {@link ModelState#Realized}.
	 *
	 * @param node  the model node that transitioned to the realized state
	 */
	void realized(ModelNode node);

	/**
	 * When the model node has new projection.
	 *
	 * @param node  the model node that has a new projection
	 * @param newComponent
	 */
	void projectionAdded(ModelNode node, Object newComponent);
}
