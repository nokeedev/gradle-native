package dev.nokee.model.graphdb;

import com.google.common.testing.NullPointerTester;
import com.spotify.hamcrest.optional.OptionalMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.graphdb.RelationshipType.withName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class RelationshipCreationIntegrationTest {
	private final Graph subject = new DefaultGraph();
	private final Node startNode = subject.createNode();
	private final Node endNode = subject.createNode();
	private final Relationship relationship = subject.createRelationship(startNode, withName("knows"), endNode);

	@Test
	void returnsNewRelationshipUponCreation() {
		assertThat(relationship, notNullValue(Relationship.class));
	}

	@Test
	void includesNewRelationshipInAllRelationships() {
		assertThat(relationship, in(subject.getAllRelationships().collect(toImmutableList())));
	}

	@Test
	void hasStartNode() {
		assertThat(relationship.getStartNode(), is(startNode));
	}

	@Test
	void hasEndNode() {
		assertThat(relationship.getEndNode(), is(endNode));
	}

	@Test
	void hasRelationshipType() {
		assertThat(relationship.getType(), is(withName("knows")));
	}

	@Test
	void canCheckRelationshipType() {
		assertThat(relationship.isType(withName("knows")), is(true));
		assertThat(relationship.isType(withName("owns")), is(false));
	}

	@Test
	void canAccessAllNodesAsArray() {
		assertThat(relationship.getNodes(), arrayContaining(startNode, endNode));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() throws NoSuchMethodException {
		new NullPointerTester()
			.testMethod(subject, Graph.class.getMethod("createRelationship", Node.class, RelationshipType.class, Node.class));
	}

	@Test
	void startNodeHasOutgoingRelationship() {
		assertThat(startNode.getSingleRelationship(withName("knows"), Direction.OUTGOING),
			optionalWithValue(is(relationship)));
	}

	@Test
	void endNodeHasIncomingRelationship() {
		assertThat(endNode.getSingleRelationship(withName("knows"), Direction.INCOMING),
			optionalWithValue(is(relationship)));
	}
}
