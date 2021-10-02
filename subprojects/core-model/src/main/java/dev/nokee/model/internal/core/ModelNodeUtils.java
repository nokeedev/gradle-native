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

import dev.nokee.model.internal.state.ModelState;

import java.util.Optional;

// TODO: Remove "maybe add" custom logic to favour dedup within ModelProjection adding logic
public final class ModelNodeUtils {
	private static final ModelComponent CREATED_TAG = new ModelState.Created();
	private static final ModelComponent REALIZED_TAG = new ModelState.Realized();
	private static final ModelComponent INITIALIZED_TAG = new ModelState.Initialized();
	private static final ModelComponent REGISTERED_TAG = new ModelState.Registered();

	private ModelNodeUtils() {}

	static ModelNode create(ModelNode self) {
		if (!self.hasComponent(ModelState.Created.class)) {
			if (self.hasComponent(ModelNode.State.class)) {
				self.setComponent(ModelNode.State.class, ModelNode.State.Created);
			} else {
				self.addComponent(ModelNode.State.Created);
			}
			self.notifyCreated();
			self.addComponent(CREATED_TAG);
		}
		return self;
	}

	/**
	 * Realize this node.
	 *
	 * @param self  the node to realize, must not be null
	 * @return this model node, never null
	 */
	public static ModelNode realize(ModelNode self) {
		if (!self.hasComponent(ModelState.Realized.class)) {
			ModelNodeUtils.register(self);
			if (!ModelNodeUtils.isAtLeast(self, ModelNode.State.Realized)) {
				changeStateToRealizeBeforeRealizingParentNodeIfPresentToAvoidDuplicateRealizedCallback(self);
				self.notifyRealized();
			}
			self.addComponent(REALIZED_TAG);
		}
		return self;
	}

	private static void changeStateToRealizeBeforeRealizingParentNodeIfPresentToAvoidDuplicateRealizedCallback(ModelNode self) {
		if (self.hasComponent(ModelNode.State.class)) {
			self.setComponent(ModelNode.State.class, ModelNode.State.Realized);
		} else {
			self.addComponent(ModelNode.State.Realized);
		}
		getParent(self).ifPresent(ModelNodeUtils::realize);
	}

	static ModelNode initialize(ModelNode self) {
		if (!self.hasComponent(ModelState.Initialized.class)) {
			assert self.getComponent(ModelNode.State.class) == ModelNode.State.Created;
			if (self.hasComponent(ModelNode.State.class)) {
				self.setComponent(ModelNode.State.class, ModelNode.State.Initialized);
			} else {
				self.addComponent(ModelNode.State.Initialized);
			}
			self.notifyInitialized();
			self.addComponent(INITIALIZED_TAG);
		}
		return self;
	}

	public static ModelNode register(ModelNode self) {
		if (!self.hasComponent(ModelState.Registered.class)) {
			if (!isAtLeast(self, ModelNode.State.Registered)) {
				if (self.hasComponent(ModelNode.State.class)) {
					self.setComponent(ModelNode.State.class, ModelNode.State.Registered);
				} else {
					self.addComponent(ModelNode.State.Registered);
				}
				self.notifyRegistered();
			}
			self.addComponent(REGISTERED_TAG);
		}
		return self;
	}

	/**
	 * Checks the state of the specified node is at or later that the specified state.
	 *
	 * @param self  the node to compare, must not be null
	 * @param state  the state to compare
	 * @return {@literal true} if the state of the node is at or later that the specified state or {@literal false} otherwise.
	 */
	public static boolean isAtLeast(ModelNode self, ModelNode.State state) {
		return self.getComponent(ModelNode.State.class).compareTo(state) >= 0;
	}

	/**
	 * Returns the state of the specified node.
	 *
	 * @param self  the node to query its state, must not be null
	 * @return a {@link ModelNode.State} representing the state of this model node, never null.
	 */
	public static ModelNode.State getState(ModelNode self) {
		return self.getComponent(ModelNode.State.class);
	}

	/**
	 * Returns the parent node of the specified node, if available.
	 *
	 * @return the parent model node, never null but can be absent
	 */
	public static Optional<ModelNode> getParent(ModelNode self) {
		return self.findComponent(ParentNode.class).map(ParentNode::get);
	}
}
