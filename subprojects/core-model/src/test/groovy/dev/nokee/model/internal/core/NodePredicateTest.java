package dev.nokee.model.internal.core;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

class NodePredicateTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(NodePredicate.class);
	}
}
