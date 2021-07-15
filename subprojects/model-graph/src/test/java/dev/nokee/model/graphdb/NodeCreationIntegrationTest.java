package dev.nokee.model.graphdb;

import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.notNullValue;

class NodeCreationIntegrationTest {
	private final Graph subject = new DefaultGraph();

	@Test
	void returnsNewNodeUponCreation() {
		assertThat(subject.createNode(), notNullValue(Node.class));
	}

	@Test
	void includesNewNodeInAllNodes() {
		assertThat(subject.createNode(), in(subject.getAllNodes().collect(toImmutableList())));
	}
}
