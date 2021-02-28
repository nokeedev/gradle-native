package dev.nokee.model.graphdb;

import dev.nokee.model.graphdb.testers.EntityTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.graphdb.RelationshipType.withName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class RelationshipEntityTest implements EntityTester {
	private final Graph graph = new DefaultGraph();

	@Override
	public Entity createEntity() {
		return graph.createRelationship(graph.createNode(), withName("knows"), graph.createNode());
	}

	@Test
	void canGetOtherNode() {
		val startNode = graph.createNode();
		val endNode = graph.createNode();
		val subject = graph.createRelationship(startNode, withName("knows"), endNode);

		assertThat(subject.getOtherNode(startNode), is(endNode));
		assertThat(subject.getOtherNode(endNode), is(startNode));
	}

//	@Nested
//	class NewRelationship {
//
//		@Test
//		void hasRelationshipType() {
//
//		}
//	}
	// relationship has type
	// has start node
	// has end node
	// can check relationship type
	// get other node
	// return node tuple



	// start node has relationship
	// end node has relationship
}
