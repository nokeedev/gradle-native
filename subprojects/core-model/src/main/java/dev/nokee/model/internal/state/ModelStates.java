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
package dev.nokee.model.internal.state;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeListener;
import dev.nokee.model.internal.core.ModelNodeUtils;

public final class ModelStates {
	private static final Object CREATED_TAG = new ModelState.IsAtLeastCreated();
	private static final Object REALIZED_TAG = new ModelState.IsAtLeastRealized();
	private static final Object INITIALIZED_TAG = new ModelState.IsAtLeastInitialized();
	private static final Object REGISTERED_TAG = new ModelState.IsAtLeastRegistered();

	private ModelStates() {}

	public static ModelNode create(ModelNode self) {
		if (!self.hasComponent(ModelState.IsAtLeastCreated.class)) {
			if (self.hasComponent(ModelState.class)) {
				self.setComponent(ModelState.class, ModelState.Created);
			} else {
				self.addComponent(ModelState.Created);
			}
			notifyCreated(self);
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
		if (!self.hasComponent(ModelState.IsAtLeastRealized.class)) {
			register(self);
			if (!isAtLeast(self, ModelState.Realized)) {
				changeStateToRealizeBeforeRealizingParentNodeIfPresentToAvoidDuplicateRealizedCallback(self);
				notifyRealized(self);
			}
			self.addComponent(REALIZED_TAG);
		}
		return self;
	}

	private static void changeStateToRealizeBeforeRealizingParentNodeIfPresentToAvoidDuplicateRealizedCallback(ModelNode self) {
		if (self.hasComponent(ModelState.class)) {
			self.setComponent(ModelState.class, ModelState.Realized);
		} else {
			self.addComponent(ModelState.Realized);
		}
		ModelNodeUtils.getParent(self).ifPresent(ModelStates::realize);
	}

	public static ModelNode initialize(ModelNode self) {
		if (!self.hasComponent(ModelState.IsAtLeastInitialized.class)) {
			create(self);
			if (self.hasComponent(ModelState.class)) {
				self.setComponent(ModelState.class, ModelState.Initialized);
			} else {
				self.addComponent(ModelState.Initialized);
			}
			notifyInitialized(self);
			self.addComponent(INITIALIZED_TAG);
		}
		return self;
	}

	public static ModelNode register(ModelNode self) {
		if (!self.hasComponent(ModelState.IsAtLeastRegistered.class)) {
			initialize(self);
			if (!isAtLeast(self, ModelState.Registered)) {
				if (self.hasComponent(ModelState.class)) {
					self.setComponent(ModelState.class, ModelState.Registered);
				} else {
					self.addComponent(ModelState.Registered);
				}
				notifyRegistered(self);
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
	public static boolean isAtLeast(ModelNode self, ModelState state) {
		return getState(self).compareTo(state) >= 0;
	}

	/**
	 * Returns the state of the specified node.
	 *
	 * @param self  the node to query its state, must not be null
	 * @return a {@link ModelState} representing the state of this model node, never null.
	 */
	public static ModelState getState(ModelNode self) {
		return self.findComponent(ModelState.class).orElse(ModelState.Created);
	}

	private static void notifyCreated(ModelNode self) {
		self.findComponent(ModelNodeListener.class).ifPresent(listener -> listener.created(self));
	}

	private static void notifyInitialized(ModelNode self) {
		self.findComponent(ModelNodeListener.class).ifPresent(listener -> listener.initialized(self));
	}

	private static void notifyRegistered(ModelNode self) {
		self.findComponent(ModelNodeListener.class).ifPresent(listener -> listener.registered(self));
	}

	private static void notifyRealized(ModelNode self) {
		self.findComponent(ModelNodeListener.class).ifPresent(listener -> listener.realized(self));
	}
}
