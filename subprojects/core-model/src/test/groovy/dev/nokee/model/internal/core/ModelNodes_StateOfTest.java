package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNodes.stateOf;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelNodes_StateOfTest {
	@Test
	void checkToString() {
		assertThat(stateOf(ModelNode.State.Registered), hasToString("ModelNodes.stateOf(Registered)"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(stateOf(ModelNode.State.Registered), stateOf(ModelNode.State.Registered))
			.addEqualityGroup(stateOf(ModelNode.State.Initialized))
			.addEqualityGroup(stateOf(ModelNode.State.Realized))
			.testEquals();
	}

	@Test
	void canCreatePredicateFilterForModelNodeForStateOfInitialized() {
		val predicate = stateOf(ModelNode.State.Initialized);
		assertTrue(predicate.test(node()));
		assertFalse(predicate.test(node().register()));
		assertFalse(predicate.test(node().realize()));
	}

	@Test
	void canCreatePredicateFilterForModelNodeForStateOfRegistered() {
		val predicate = stateOf(ModelNode.State.Registered);
		assertFalse(predicate.test(node()));
		assertTrue(predicate.test(node().register()));
		assertFalse(predicate.test(node().realize()));
	}

	@Test
	void canCreatePredicateFilterForModelNodeByStateAtLeastRealized() {
		val predicate = stateOf(ModelNode.State.Realized);
		assertFalse(predicate.test(node()));
		assertFalse(predicate.test(node().register()));
		assertTrue(predicate.test(node().realize()));
	}
}
