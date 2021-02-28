package dev.nokee.model.graphdb;

import lombok.val;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static dev.nokee.model.graphdb.RelationshipType.withName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultGraphTest {
	private final Graph subject = new DefaultGraph();

	@Test
	void newGraphHasNoNodes() {
		assertThat(subject.getAllNodes().count(), is(0L));
	}

	@Test
	void newGraphHasNoRelationships() {
		assertThat(subject.getAllRelationships().count(), is(0L));
	}

	@Test
	void allNodesAreAccessibleFromGraph() {
		val node = subject.createNode();
		assertThat(subject.getAllNodes().collect(toImmutableList()), contains(node));
	}

	@Test
	void allRelationshipAreAccessibleFromGraph() {
		val startNode = subject.createNode();
		val endNode = subject.createNode();
		val relationship = subject.createRelationship(startNode, withName("owns"), endNode);
		assertThat(subject.getAllRelationships().collect(toImmutableList()), contains(relationship));
	}

	@Test
	void nodesAreReturnedInCreatedOrder() {
		val n0 = subject.createNode();
		val n1 = subject.createNode();
		val n2 = subject.createNode();
		assertThat(subject.getAllNodes().collect(toImmutableList()), contains(n0, n1, n2));
	}

	@Test
	void relationshipsAreReturnedInCreatedOrder() {
		val startNode = subject.createNode();
		val endNode = subject.createNode();
		val r0 = subject.createRelationship(startNode, withName("owns"), endNode);
		val r1 = subject.createRelationship(startNode, withName("knows"), endNode);
		val r2 = subject.createRelationship(startNode, withName("follows"), endNode);
		assertThat(subject.getAllRelationships().collect(toImmutableList()), contains(r0, r1, r2));
	}

	@Test
	void canAccessNodeById() {
		val node = subject.createNode();
		assertAll(
			() -> assertThat(subject.getNodeById(node.getId()), is(node)),
			() -> assertThat(subject.getEntityById(node.getId()), is(node)),
			() -> assertThrows(NotFoundException.class, () -> subject.getNodeById(node.getId() + 1)),
			() -> assertThrows(NotFoundException.class, () -> subject.getRelationshipById(node.getId()))
		);
	}

	@Test
	void canAccessRelationshipById() {
		val startNode = subject.createNode();
		val endNode = subject.createNode();
		val relationship = subject.createRelationship(startNode, withName("owns"), endNode);
		assertAll(
			() -> assertThat(subject.getEntityById(relationship.getId()), is(relationship)),
			() -> assertThat(subject.getRelationshipById(relationship.getId()), is(relationship)),
			() -> assertThrows(NotFoundException.class, () -> subject.getNodeById(relationship.getId())),
			() -> assertThrows(NotFoundException.class, () -> subject.getRelationshipById(relationship.getId() + 1))
		);
	}
}
