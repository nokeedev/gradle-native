package dev.nokee.model.graphdb;

import com.google.common.testing.NullPointerTester;
import dev.nokee.model.graphdb.testers.EntityTester;
import dev.nokee.model.graphdb.testers.NodeLabelTester;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.graphdb.Direction.*;
import static dev.nokee.model.graphdb.Label.label;
import static dev.nokee.model.graphdb.RelationshipType.withName;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NodeEntityTest implements EntityTester, NodeLabelTester {
	private final Graph graph = new DefaultGraph();

	@Override
	public Entity createEntity() {
		return graph.createNode();
	}

	@Override
	public Node createSubject() {
		return graph.createNode();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() throws NoSuchMethodException {
		new NullPointerTester()
			// null default value is allowed
			.ignore(AbstractEntity.class.getMethod("getProperty", String.class, Object.class))

			// null value is allowed, for now
			.ignore(Entity.class.getMethod("setProperty", String.class, Object.class))
			.ignore(AbstractEntity.class.getMethod("setProperty", String.class, Object.class))
			.ignore(Entity.class.getMethod("property", String.class, Object.class))
			.ignore(NodeEntity.class.getMethod("property", String.class, Object.class))
			.ignore(AbstractEntity.class.getMethod("property", String.class, Object.class))
			.ignore(RelationshipEntity.class.getMethod("property", String.class, Object.class))
			.testAllPublicInstanceMethods(createEntity());
	}

	@Nested
	class EmptyNode {
		private final RelationshipType ANY_TYPE = withName("knows");
		private final Label ANY_LABEL = label("component");
		private final Node subject = graph.createNode();

		@Test
		void hasNoRelationships() {
			assertAll(
				() -> assertThat(subject.getRelationships().findAny(), emptyOptional()),
				() -> assertThat(subject.getRelationships(BOTH).findAny(), emptyOptional()),
				() -> assertThat(subject.getRelationships(INCOMING).findAny(), emptyOptional()),
				() -> assertThat(subject.getRelationships(OUTGOING).findAny(), emptyOptional()),
				() -> assertThat(subject.getRelationships(BOTH, ANY_TYPE).findAny(), emptyOptional()),
				() -> assertThat(subject.getRelationships(INCOMING, ANY_TYPE).findAny(), emptyOptional()),
				() -> assertThat(subject.getRelationships(OUTGOING, ANY_TYPE).findAny(), emptyOptional()),
				() -> assertThat(subject.getRelationships(ANY_TYPE).findAny(), emptyOptional())
			);
		}

		@Test
		void canCheckNoRelationshipsExists() {
			assertAll(
				() -> assertThat(subject.hasRelationship(), is(false)),
				() -> assertThat(subject.hasRelationship(BOTH), is(false)),
				() -> assertThat(subject.hasRelationship(INCOMING), is(false)),
				() -> assertThat(subject.hasRelationship(OUTGOING), is(false)),
				() -> assertThat(subject.hasRelationship(BOTH, ANY_TYPE), is(false)),
				() -> assertThat(subject.hasRelationship(INCOMING, ANY_TYPE), is(false)),
				() -> assertThat(subject.hasRelationship(OUTGOING, ANY_TYPE), is(false)),
				() -> assertThat(subject.hasRelationship(ANY_TYPE), is(false))
			);
		}

		@Test
		void returnsEmptyOptionalWhenGettingSingleRelationship() {
			assertThat(subject.getSingleRelationship(ANY_TYPE, BOTH), emptyOptional());
			assertThat(subject.getSingleRelationship(ANY_TYPE, INCOMING), emptyOptional());
			assertThat(subject.getSingleRelationship(ANY_TYPE, OUTGOING), emptyOptional());
		}

		@Test
		void hasNoLabel() {
			assertThat(subject.getLabels().findAny(), emptyOptional());
			assertThat(subject.hasLabel(ANY_LABEL), is(false));
		}
	}

	@Nested
	class NodeRelationship {
		private Node incomingRelationships(Node node) {
			graph.createRelationship(graph.createNode(), withName("knows"), node);
			graph.createRelationship(graph.createNode(), withName("knows"), node);
			graph.createRelationship(graph.createNode(), withName("owns"), node);
			graph.createRelationship(graph.createNode(), withName("uses"), node);
			return node;
		}

		private Node outgoingRelationships(Node node) {
			graph.createRelationship(node, withName("knows"), graph.createNode());
			graph.createRelationship(node, withName("owns"), graph.createNode());
			graph.createRelationship(node, withName("owns"), graph.createNode());
			graph.createRelationship(node, withName("uses"), graph.createNode());
			return node;
		}

		@Test
		void canCheckExistenceOfAnyIncomingRelationships() {
			val subject = incomingRelationships(graph.createNode());
			assertAll(
				() -> assertThat(subject.hasRelationship(), is(true)),
				() -> assertThat(subject.hasRelationship(withName("knows")), is(true)),
				() -> assertThat(subject.hasRelationship(BOTH), is(true)),
				() -> assertThat(subject.hasRelationship(BOTH, withName("owns")), is(true)),
				() -> assertThat(subject.hasRelationship(INCOMING), is(true)),
				() -> assertThat(subject.hasRelationship(INCOMING, withName("uses")), is(true)),

				() -> assertThat(subject.hasRelationship(OUTGOING), is(false)),
				() -> assertThat(subject.hasRelationship(OUTGOING, withName("knows")), is(false)),
				() -> assertThat(subject.hasRelationship(withName("unknown")), is(false)),
				() -> assertThat(subject.hasRelationship(BOTH, withName("unknown")), is(false)),
				() -> assertThat(subject.hasRelationship(INCOMING, withName("unknown")), is(false))
			);
		}

		@Test
		void canCheckExistenceOfAnyOutgoingRelationships() {
			val subject = outgoingRelationships(graph.createNode());
			assertAll(
				() -> assertThat(subject.hasRelationship(), is(true)),
				() -> assertThat(subject.hasRelationship(withName("knows")), is(true)),
				() -> assertThat(subject.hasRelationship(BOTH), is(true)),
				() -> assertThat(subject.hasRelationship(BOTH, withName("owns")), is(true)),
				() -> assertThat(subject.hasRelationship(OUTGOING), is(true)),
				() -> assertThat(subject.hasRelationship(OUTGOING, withName("knows")), is(true)),

				() -> assertThat(subject.hasRelationship(INCOMING), is(false)),
				() -> assertThat(subject.hasRelationship(INCOMING, withName("uses")), is(false)),
				() -> assertThat(subject.hasRelationship(withName("unknown")), is(false)),
				() -> assertThat(subject.hasRelationship(BOTH, withName("unknown")), is(false)),
				() -> assertThat(subject.hasRelationship(INCOMING, withName("unknown")), is(false))
			);
		}

		@Test
		void canAccessRelationships() {
			val subject = incomingRelationships(outgoingRelationships(graph.createNode()));
			assertAll(
				() -> assertThat(subject.getRelationships().count(), is(8L)),
				() -> assertThat(subject.getRelationships(BOTH).count(), is(8L)),
				() -> assertThat(subject.getRelationships(BOTH, withName("knows")).count(), is(3L)),
				() -> assertThat(subject.getRelationships(INCOMING).count(), is(4L)),
				() -> assertThat(subject.getRelationships(INCOMING, withName("owns")).count(), is(1L)),
				() -> assertThat(subject.getRelationships(OUTGOING).count(), is(4L)),
				() -> assertThat(subject.getRelationships(OUTGOING, withName("uses")).count(), is(1L)),
				() -> assertThat(subject.getRelationships(withName("owns")).count(), is(3L)),
				() -> assertThat(subject.getRelationships(withName("owns"), withName("uses")).count(), is(5L))
			);
		}

		@Test
		void canGetSingleRelationship() {
			val subject = incomingRelationships(outgoingRelationships(graph.createNode()));
			assertThat(subject.getSingleRelationship(withName("uses"), INCOMING), optionalWithValue());
			assertThat(subject.getSingleRelationship(withName("knows"), OUTGOING), optionalWithValue());
		}

		@Test
		void throwsExceptionWhenGettingSingleRelationshipButHasMultiple() {
			val subject = incomingRelationships(outgoingRelationships(graph.createNode()));
			assertThrows(RuntimeException.class, () -> subject.getSingleRelationship(withName("knows"), INCOMING));
			assertThrows(RuntimeException.class, () -> subject.getSingleRelationship(withName("owns"), OUTGOING));
		}

		@Test
		void canCreateRelationshipToAnotherNode() {
			val subject = graph.createNode();
			val relationship = subject.createRelationshipTo(graph.createNode(), withName("knows"));
			assertThat(relationship, notNullValue(Relationship.class));
			assertThat(subject.hasRelationship(), is(true));
			assertThat(graph.getAllRelationships().count(), is(1L));
		}
	}

	@Nested
	class NodeLabel {
		@Test
		void canAddLabels() {
			val subject = graph.createNode();
			subject.addLabel(label("component"));
			assertThat(subject.getLabels().count(), is(1L));
		}

		@Test
		void canGetAllLabels() {
			val subject = graph.createNode();
			subject.addLabel(label("component"));
			subject.addLabel(label("application"));
			assertThat(subject.getLabels().collect(toList()), contains(label("component"), label("application")));
		}

		@Test
		void canCheckLabels() {
			val subject = graph.createNode();
			subject.addLabel(label("variant"));
			assertThat(subject.hasLabel(label("variant")), is(true));
			assertThat(subject.hasLabel(label("missing")), is(false));
		}

		@Test
		void ignoresDuplicateLabel() {
			val subject = graph.createNode();
			subject.addLabel(label("foo"));
			subject.addLabel(label("foo"));
			assertThat(subject.getLabels().collect(toList()), contains(label("foo")));
		}
	}
}
