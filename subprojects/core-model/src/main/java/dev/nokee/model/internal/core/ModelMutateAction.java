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

import dev.nokee.model.internal.type.ModelType;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class ModelMutateAction implements ModelAction {
	@Override
	public void execute(ModelNode node) {
		if (ModelNodeUtils.getState(node).equals(ModelNode.State.Realized)) {
			ModelNodeContext.of(node).execute(() -> execute(new Context(node)));
		}
	}

	public abstract void execute(Context context);

	public static class Context {
		private final ModelNode node;

		public Context(ModelNode node) {
			this.node = node;
		}

		public ModelPath getPath() {
			return node.getPath();
		}

		public Context applyTo(NodeAction action) {
			node.applyTo(action);
			return this;
		}

		public <T> T projectionOf(ModelType<T> type) {
			checkArgument(ModelNodeUtils.canBeViewedAs(node, type));
			return ModelNodeUtils.get(node, type);
		}
	}
}
