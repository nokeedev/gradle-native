package dev.nokee.model.graphdb;

import com.google.common.base.Predicates;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class NodeEntity extends AbstractEntity implements Node {
	final List<Relationship> relationshipList = new ArrayList<>();
	private final NodeLabels labels;
	private final Graph graph;

	NodeEntity(EntityProperties properties, NodeLabels labels, Graph graph) {
		super(properties);
		this.labels = labels;
		this.graph = graph;
	}

	@Override
	public Relationship createRelationshipTo(Node otherNode, RelationshipType type) {
		return graph.createRelationship(this, type, otherNode);
	}

	@Override
	public Stream<Relationship> getRelationships() {
		return relationshipList.stream();
	}

	@Override
	public Stream<Relationship> getRelationships(Direction direction) {
		return relationshipList.stream().filter(forDirection(direction));
	}

	@Override
	public Stream<Relationship> getRelationships(Direction direction, RelationshipType... types) {
		return relationshipList.stream().filter(forDirection(direction).and(forTypes(types)));
	}

	@Override
	public Stream<Relationship> getRelationships(RelationshipType... types) {
		return relationshipList.stream().filter(forTypes(types));
	}

	@Override
	public boolean hasRelationship() {
		return !relationshipList.isEmpty();
	}

	@Override
	public boolean hasRelationship(Direction direction) {
		return relationshipList.stream().anyMatch(forDirection(direction));
	}

	private Predicate<Relationship> forDirection(Direction direction) {
		switch (direction) {
			case BOTH: return Predicates.alwaysTrue();
			case INCOMING: return it -> it.getEndNode().equals(this);
			case OUTGOING: return it -> it.getStartNode().equals(this);
		}
		throw new UnsupportedOperationException(String.format("Unsupported direction value '%s'. Supported direction values: %s", direction, Arrays.toString(Direction.values())));
	}

	private static Predicate<Relationship> forTypes(RelationshipType... types) {
		if (types.length == 0) {
			return Predicates.alwaysTrue();
		}
		return it -> Arrays.stream(types).anyMatch(it::isType);
	}

	@Override
	public boolean hasRelationship(Direction direction, RelationshipType... types) {
		return relationshipList.stream().anyMatch(forDirection(direction).and(forTypes(types)));
	}

	@Override
	public boolean hasRelationship(RelationshipType... types) {
		return relationshipList.stream().anyMatch(forTypes(types));
	}

	@Override
	public Optional<Relationship> getSingleRelationship(RelationshipType type, Direction direction) {
		requireNonNull(type);
		requireNonNull(direction);
		val iter = getRelationships(direction, type).iterator();
		if (!iter.hasNext()) {
			return Optional.empty();
		}
		val relationship = iter.next();
		if (iter.hasNext()) {
			throw new RuntimeException(String.format("More than one relationship matches the given type (%s) and direction (%s).", type, direction));
		}
		return Optional.of(relationship);
	}

	@Override
	public Node addLabel(Label label) {
		labels.add(label);
		return this;
	}

	@Override
	public Stream<Label> getLabels() {
		return labels.stream();
	}

	@Override
	public boolean hasLabel(Label label) {
		return labels.has(label);
	}

	@Override
	public Node property(String key, Object value) {
		super.property(key, value);
		return this;
	}
}
