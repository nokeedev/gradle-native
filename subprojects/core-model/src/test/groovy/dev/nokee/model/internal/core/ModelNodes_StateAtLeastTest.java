package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.*;

public class ModelNodes_StateAtLeastTest {
	@Test
	void checkToString() {
		assertThat(stateAtLeast(ModelNode.State.Registered), hasToString("ModelNodes.stateAtLeast(Registered)"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(stateAtLeast(ModelNode.State.Registered), stateAtLeast(ModelNode.State.Registered))
			.addEqualityGroup(stateAtLeast(ModelNode.State.Initialized))
			.addEqualityGroup(stateAtLeast(ModelNode.State.Realized))
			.testEquals();
	}

	@Test
	void canCreatePredicateFilterForModelNodeByStateAtLeastInitialized() {
		val predicate = stateAtLeast(ModelNode.State.Initialized);
		assertAll(() -> {
			assertTrue(predicate.test(node()));
			assertTrue(predicate.test(node().register()));
			assertTrue(predicate.test(node().realize()));
		});
	}

	@Test
	void canCreatePredicateFilterForModelNodeByStateAtLeastRegistered() {
		val predicate = stateAtLeast(ModelNode.State.Registered);
		assertAll(() -> {
			assertFalse(predicate.test(node()));
			assertTrue(predicate.test(node().register()));
			assertTrue(predicate.test(node().realize()));
		});
	}

	@Test
	void canCreatePredicateFilterForModelNodeByStateAtLeastRealized() {
		val predicate = stateAtLeast(ModelNode.State.Realized);
		assertAll(() -> {
			assertFalse(predicate.test(node()));
			assertFalse(predicate.test(node().register()));
			assertTrue(predicate.test(node().realize()));
		});
	}
}
