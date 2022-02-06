/*
 * Copyright 2020-2021 the original author or authors.
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

import com.google.common.collect.ImmutableList;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.DefaultKnownDomainObject;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class ModelInitializerAction implements ModelAction, HasInputs {
	private final List<ModelComponentReference<?>> inputs = ImmutableList.of(ModelComponentReference.of(ModelState.class), ModelComponentReference.of(BindManagedProjectionService.class));

	@Override
	public final void execute(ModelNode node) {
		if (ModelStates.getState(node).equals(ModelState.Created)) {
			// NOTE: The contextual node should not be accessed from the action, it's simply for contextualizing the action execution.
			ModelNodeContext.of(node).execute(() -> execute(new Context(node)));
		}
	}

	@Override
	public List<? extends ModelComponentReference<?>> getInputs() {
		return inputs;
	}

	public abstract void execute(Context context);

	public static class Context {
		private final ModelNode node;

		public Context(ModelNode node) {
			this.node = node;
		}

		public ModelPath getPath() {
			return ModelNodeUtils.getPath(node);
		}

		public Context applyTo(NodeAction action) {
			ModelNodeUtils.applyTo(node, action);
			return this;
		}

		public Context withProjection(ModelProjection projection) {
			assert ModelStates.getState(node) == ModelState.Created : "can only add projection before the node is initialized";
			node.addComponent(node.getComponent(BindManagedProjectionService.class).bindManagedProjectionWithInstantiator(projection));
			return this;
		}

		public <T> KnownDomainObject<T> withProjection(TypeCompatibilityModelProjectionSupport<T> projection) {
			withProjection((ModelProjection) projection);
			return DefaultKnownDomainObject.of(projection.getType(), node);
		}

		public <T> KnownDomainObject<T> projectionOf(ModelType<T> type) {
			checkArgument(ModelNodeUtils.canBeViewedAs(node, type));
			return DefaultKnownDomainObject.of(type, node);
		}
	}
}
