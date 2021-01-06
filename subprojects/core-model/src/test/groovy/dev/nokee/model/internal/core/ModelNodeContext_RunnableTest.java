package dev.nokee.model.internal.core;

import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static dev.nokee.internal.testing.ExecuteWith.*;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(ModelNodeContext.class)
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
