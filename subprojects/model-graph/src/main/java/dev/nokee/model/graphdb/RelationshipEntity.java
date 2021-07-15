package dev.nokee.model.graphdb;

import static java.util.Objects.requireNonNull;

final class RelationshipEntity extends AbstractEntity implements Relationship {
	private final Node startNode;
	private final RelationshipType type;
	private final Node endNode;

	RelationshipEntity(EntityProperties properties, Node from, RelationshipType type, Node to) {
		super(properties);
		this.startNode = requireNonNull(from);
		this.type = requireNonNull(type);
		this.endNode = requireNonNull(to);
	}

	@Override
	public Node getStartNode() {
		return startNode;
	}

	@Override
	public Node getOtherNode(Node node) {
		if (node.equals(startNode)) {
			return endNode;
		}
		return startNode;
	}

	@Override
	public Node getEndNode() {
		return endNode;
	}

	@Override
	public RelationshipType getType() {
		return type;
	}

	@Override
	public boolean isType(RelationshipType type) {
		return this.type.equals(type);
	}

	@Override
	public Node[] getNodes() {
		return new Node[] { startNode, endNode };
	}

	@Override
	public Relationship property(String key, Object value) {
		super.property(key, value);
		return this;
	}
}
