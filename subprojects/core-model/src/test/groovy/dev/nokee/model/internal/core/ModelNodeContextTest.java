package dev.nokee.model.internal.core;

import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Functions.constant;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static java.util.function.Function.identity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(ModelNodeContext.class)
class ModelNodeContextTest {
	private final ModelNode node = node("x.y.z");
	private final ModelNodeContext subject = ModelNodeContext.of(node);

	@Test
	void canAccessCurrentModelNodeWhileExecutingInContext() {
		val captor = subject.execute(new CaptureModelNodeContextFunction());
		assertThat(captor.getNode(), equalTo(node));
	}

	static class CaptureModelNodeContextFunction implements Function<ModelNode, CaptureModelNodeContextFunction> {
		private ModelNode node;

		@Override
		public CaptureModelNodeContextFunction apply(ModelNode modelNode) {
			node = ModelNodeContext.getCurrentModelNode();
			return this;
		}

		public ModelNode getNode() {
			return node;
		}
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
		assertThrows(UnsupportedOperationException.class, () -> subject.execute(node -> { throw new UnsupportedOperationException(); }));
		assertThrows(NullPointerException.class, ModelNodeContext::getCurrentModelNode);
	}

	@Test
	void canExecuteNestedContext() {
		val nestedNode = node("a.b.c");
		val captor = subject.execute(new CaptureNestedModelNodeContextFunction(nestedNode));
		assertThat(captor.getAllValues(), contains(node, nestedNode, node));
	}

	static class CaptureNestedModelNodeContextFunction implements Function<ModelNode, CaptureNestedModelNodeContextFunction> {
		private final List<ModelNode> allValues = new ArrayList<>();
		private final ModelNode nestedNode;

		public CaptureNestedModelNodeContextFunction(ModelNode nestedNode) {
			this.nestedNode = nestedNode;
		}

		@Override
		public CaptureNestedModelNodeContextFunction apply(ModelNode modelNode) {
			allValues.add(ModelNodeContext.getCurrentModelNode());
			allValues.add(ModelNodeContext.of(nestedNode).execute(new CaptureModelNodeContextFunction()).getNode());
			allValues.add(ModelNodeContext.getCurrentModelNode());
			return this;
		}

		public List<ModelNode> getAllValues() {
			return allValues;
		}
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(ModelNodeContext.class);
	}
}
