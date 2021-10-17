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

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelNodeContext_RunnableTest {
	private final ModelNode node = node("x.y.z");
	private final ModelNodeContext subject = ModelNodeContext.of(node);

	@Test
	void calledWithCurrentModelNode() {
		assertThat(executeWith(runnable(subject::execute)), calledOnce());
	}

	@Test
	void canAccessCurrentModelNodeWhileExecutingInContext() {
		val contextualNodeCaptor = contextualCapture(ModelNodeContext::getCurrentModelNode);
		executeWith(runnable(subject::execute).captureUsing(contextualNodeCaptor));
		assertThat(contextualNodeCaptor.getLastValue(), equalTo(node));
	}

	@Test
	void cannotAccessCurrentModelNodeAfterContextIsExecution() {
		executeWith(runnable(subject::execute));
		assertThrows(NullPointerException.class, ModelNodeContext::getCurrentModelNode);
	}

	@Test
	void cannotAccessCurrentModelNodeWhenExceptionThrowDuringContextExecution() {
		assertThrows(RuntimeException.class,
			() -> executeWith(runnable(subject::execute).thenThrow(new RuntimeException("Expected exception"))));
		assertThrows(NullPointerException.class, ModelNodeContext::getCurrentModelNode);
	}

	@Test
	void canExecuteNestedContext() {
		val nestedNode = node("a.b.c");
		val contextualNodesCaptor = contextualCapture(new NestedModelNodeContextCaptor(nestedNode));
		executeWith(runnable(subject::execute).captureUsing(contextualNodesCaptor));
		assertThat(contextualNodesCaptor.getLastValue(), contains(node, nestedNode, node));
	}

	static class NestedModelNodeContextCaptor implements Supplier<List<ModelNode>> {
		private final ModelNode nestedNode;

		NestedModelNodeContextCaptor(ModelNode nestedNode) {
			this.nestedNode = nestedNode;
		}

		@Override
		public List<ModelNode> get() {
			List<ModelNode> allValues = new ArrayList<>();
			allValues.add(ModelNodeContext.getCurrentModelNode());
			ModelNodeContext.of(nestedNode).execute(() -> {
				allValues.add(ModelNodeContext.getCurrentModelNode());
			});
			allValues.add(ModelNodeContext.getCurrentModelNode());
			return allValues;
		}
	}
}
