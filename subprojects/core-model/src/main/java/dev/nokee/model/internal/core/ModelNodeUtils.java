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
import dev.nokee.model.internal.type.ModelType;

import java.util.Optional;

// TODO: Remove "maybe add" custom logic to favour dedup within ModelProjection adding logic
public final class ModelNodeUtils {
	private static final ModelProjection CREATED_TAG = ModelProjections.ofInstance(new ModelState.Created() {});
	private static final ModelProjection REALIZED_TAG = ModelProjections.ofInstance(new ModelState.Realized() {});
	private static final ModelProjection INITIALIZED_TAG = ModelProjections.ofInstance(new ModelState.Initialized() {});
	private static final ModelProjection REGISTERED_TAG = ModelProjections.ofInstance(new ModelState.Registered() {});

	private ModelNodeUtils() {}

	static ModelNode create(ModelNode self) {
		if (!self.canBeViewedAs(ModelType.of(ModelState.Created.class))) {
			if (self.canBeViewedAs(ModelType.of(ModelNode.State.class))) {
				self.set(ModelType.of(ModelNode.State.class), ModelNode.State.Created);
			} else {
				self.add(ModelProjections.ofInstance(ModelNode.State.Created));
			}
			self.notifyCreated();
			self.add(CREATED_TAG);
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
		if (!self.canBeViewedAs(ModelType.of(ModelState.Realized.class))) {
			ModelNodeUtils.register(self);
			if (!ModelNodeUtils.isAtLeast(self, ModelNode.State.Realized)) {
				changeStateToRealizeBeforeRealizingParentNodeIfPresentToAvoidDuplicateRealizedCallback(self);
				self.notifyRealized();
			}
			self.add(REALIZED_TAG);
		}
		return self;
	}

	private static void changeStateToRealizeBeforeRealizingParentNodeIfPresentToAvoidDuplicateRealizedCallback(ModelNode self) {
		if (self.canBeViewedAs(ModelType.of(ModelNode.State.class))) {
			self.set(ModelType.of(ModelNode.State.class), ModelNode.State.Realized);
		} else {
			self.add(ModelProjections.ofInstance(ModelNode.State.Realized));
		}
		self.getParent().ifPresent(ModelNodeUtils::realize);
	}

	static ModelNode initialize(ModelNode self) {
		if (!self.canBeViewedAs(ModelType.of(ModelState.Initialized.class))) {
			assert self.get(ModelNode.State.class) == ModelNode.State.Created;
			if (self.canBeViewedAs(ModelType.of(ModelNode.State.class))) {
				self.set(ModelType.of(ModelNode.State.class), ModelNode.State.Initialized);
			} else {
				self.add(ModelProjections.ofInstance(ModelNode.State.Initialized));
			}
			self.notifyInitialized();
			self.add(INITIALIZED_TAG);
		}
		return self;
	}

	public static ModelNode register(ModelNode self) {
		if (!self.canBeViewedAs(ModelType.of(ModelState.Registered.class))) {
			if (!isAtLeast(self, ModelNode.State.Registered)) {
				if (self.canBeViewedAs(ModelType.of(ModelNode.State.class))) {
					self.set(ModelType.of(ModelNode.State.class), ModelNode.State.Registered);
				} else {
					self.add(ModelProjections.ofInstance(ModelNode.State.Registered));
				}
				self.notifyRegistered();
			}
			self.add(REGISTERED_TAG);
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
		return self.get(ModelNode.State.class).compareTo(state) >= 0;
	}

	/**
	 * Returns the state of the specified node.
	 *
	 * @param self  the node to query its state, must not be null
	 * @return a {@link ModelNode.State} representing the state of this model node, never null.
	 */
	public static ModelNode.State getState(ModelNode self) {
		return self.get(ModelNode.State.class);
	}

	/**
	 * Returns the parent node of the specified node, if available.
	 *
	 * @return the parent model node, never null but can be absent
	 */
	public static Optional<ModelNode> getParent(ModelNode self) {
		return self.getParent();
	}
}
