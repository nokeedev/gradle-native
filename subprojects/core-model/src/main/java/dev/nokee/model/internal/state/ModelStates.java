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

import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import lombok.val;

import javax.annotation.Nullable;

public final class ModelStates {
	private static final ModelState.IsAtLeastRealized REALIZED_TAG = new ModelState.IsAtLeastRealized();
	private static final ModelState.IsAtLeastRegistered REGISTERED_TAG = new ModelState.IsAtLeastRegistered();
	private static final ModelState.IsAtLeastFinalized FINALIZED_TAG = new ModelState.IsAtLeastFinalized();

	private ModelStates() {}

	/**
	 * Realize this node.
	 *
	 * @param self  the node to realize, must not be null
	 * @return this model node, never null
	 */
	public static ModelNode realize(ModelNode self) {
		if (!self.has(ModelState.IsAtLeastRealized.class)) {
			register(self);
			if (!isAtLeast(self, ModelState.Realized)) {
				if (!self.has(Realizing.class)) {
					self.addComponent(new Realizing());
					useRealizingComponentBeforeRealizingParentNodeIfPresentToAvoidDuplicateRealizedCallback(self);
				}
			}
			self.addComponent(REALIZED_TAG);
		}
		return self;
	}

	private static final class Realizing implements ModelComponent {}

	private static void useRealizingComponentBeforeRealizingParentNodeIfPresentToAvoidDuplicateRealizedCallback(ModelNode self) {
		ModelNodeUtils.getParent(self).ifPresent(ModelStates::realize);
		if (self.has(ModelState.class)) {
			self.setComponent(ModelState.class, ModelState.Realized);
		} else {
			self.addComponent(ModelState.Realized);
		}
	}

	public static ModelNode register(ModelNode self) {
		if (!self.has(ModelState.IsAtLeastRegistered.class)) {
			if (!isAtLeast(self, ModelState.Registered)) {
				if (!self.has(Registering.class)) {
					self.addComponent(new Registering());
					if (self.has(ModelState.class)) {
						if (!self.get(ModelState.class).isAtLeast(ModelState.Registered)) {
							self.setComponent(ModelState.class, ModelState.Registered);
						}
					} else {
						self.addComponent(ModelState.Registered);
					}
				}
			}
			self.addComponent(REGISTERED_TAG);
		}
		return self;
	}

	private static final class Registering implements ModelComponent {}

	public static ModelNode finalize(ModelNode self) {
		if (!self.has(ModelState.IsAtLeastFinalized.class)) {
			realize(self);
			if (!isAtLeast(self, ModelState.Finalized)) {
				if (self.has(ModelState.class)) {
					self.setComponent(ModelState.class, ModelState.Finalized);
				} else {
					self.addComponent(ModelState.Finalized);
				}
			}
			self.addComponent(FINALIZED_TAG);
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
		val selfState = getState(self);
		if (selfState == null) {
			return false;
		} else {
			return selfState.isAtLeast(state);
		}
	}

	/**
	 * Returns the state of the specified node.
	 *
	 * @param self  the node to query its state, must not be null
	 * @return a {@link ModelState} representing the state of this model node, never null.
	 */
	@Nullable
	public static ModelState getState(ModelNode self) {
		return self.find(ModelState.class).orElse(null);
	}
}
