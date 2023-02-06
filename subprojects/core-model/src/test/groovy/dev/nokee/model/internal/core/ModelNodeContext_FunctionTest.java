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

import dev.nokee.internal.testing.testdoubles.Captor;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Functions.constant;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnce;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.withCaptured;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doThrow;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofFunction;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static java.util.function.Function.identity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelNodeContext_FunctionTest {
	private final ModelNode node = node("x.y.z");
	private final ModelNodeContext subject = ModelNodeContext.of(node);

	@Test
	void calledWithCurrentModelNode() {
		assertThat(newMock(ofFunction(ModelNode.class, ModelNode.class)).executeWith(it -> subject.execute(it)).to(method(Function<ModelNode, ModelNode>::apply)), calledOnceWith(node));
	}

	@Test
	void canAccessCurrentModelNodeWhileExecutingInContext() {
		val function = newMock(ofFunction(ModelNode.class, ModelNode.class)).when(any(callTo(method(Function<ModelNode, ModelNode>::apply))).capture(ModelNodeContext::getCurrentModelNode)).executeWith(it -> subject.execute(it));
		assertThat(function.to(method(Function<ModelNode, ModelNode>::apply)), calledOnce(withCaptured(node)));
	}

	@Test
	void canReturnNullFromContextExecution() {
		assertThat(subject.execute(constant(null)), nullValue());
	}

	@Test
	void cannotAccessCurrentModelNodeAfterContextIsExecution() {
		subject.execute(identity());
		assertThrows(NullPointerException.class, ModelNodeContext::getCurrentModelNode);
	}

	@Test
	void cannotAccessCurrentModelNodeWhenExceptionThrowDuringContextExecution() {
		assertThrows(RuntimeException.class,
			() -> newMock(ofFunction(ModelNode.class, Object.class)).when(any(callTo(method(Function<ModelNode, Object>::apply))).then(doThrow(new RuntimeException("Expected exception")))).executeWith(it -> subject.execute(it)));
		assertThrows(NullPointerException.class, ModelNodeContext::getCurrentModelNode);
	}

	@Test
	void canExecuteNestedContext() {
		val nestedNode = node("a.b.c");
		val function = newMock(ofFunction(ModelNode.class, Object.class)).when(any(callTo(method(Function<ModelNode, Object>::apply))).capture(new NestedModelNodeContextCaptor(nestedNode))).executeWith(it -> subject.execute(it));
		assertThat(function.to(method(Function<ModelNode, Object>::apply)), calledOnce(withCaptured(contains(node, nestedNode, node))));
	}

	static class NestedModelNodeContextCaptor implements Captor<List<ModelNode>> {
		private final ModelNode nestedNode;

		NestedModelNodeContextCaptor(ModelNode nestedNode) {
			this.nestedNode = nestedNode;
		}

		@Override
		public List<ModelNode> capture() {
			val allValues = new ArrayList<ModelNode>();
			allValues.add(ModelNodeContext.getCurrentModelNode());
			ModelNodeContext.of(nestedNode).execute(modelNode -> {
				allValues.add(ModelNodeContext.getCurrentModelNode());
				return null;
			});
			allValues.add(ModelNodeContext.getCurrentModelNode());
			return allValues;
		}
	}
}
