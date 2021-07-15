package dev.nokee.model.graphdb;

import dev.nokee.model.graphdb.events.EventListener;
import lombok.val;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.base.Predicates.instanceOf;
import static dev.nokee.model.graphdb.NotFoundException.*;

final class DefaultGraph implements Graph {
	private final Map<Long, Entity> entities = new HashMap<>();
	private final List<Node> nodeList = new ArrayList<>();
	private final List<Relationship> relationshipList = new ArrayList<>();
	private final GraphEventNotifier notifier;
	private long id = 0;

	public DefaultGraph() {
		this.notifier = GraphEventNotifier.noOpNotifier();
	}

	public DefaultGraph(EventListener eventListener) {
		this.notifier = GraphEventNotifier.builder().graph(this).listener(eventListener).build();
	}

	private long nextId() {
		return id++;
	}

	@Override
	public Node createNode() {
		val id = nextId();
		val node = new NodeEntity(createProperties(id), createLabels(id), this);
		nodeList.add(node);
		entities.put(node.getId(), node);
		notifier.fireNodeCreatedEvent(builder -> builder.nodeId(node.getId()));
		return node;
	}

	@Override
	public Relationship createRelationship(Node from, RelationshipType type, Node to) {
		val relationship = new RelationshipEntity(createProperties(nextId()), from, type, to);
		((NodeEntity) from).relationshipList.add(relationship);
		((NodeEntity) to).relationshipList.add(relationship);
		relationshipList.add(relationship);
		entities.put(relationship.getId(), relationship);
		notifier.fireRelationshipCreatedEvent(builder -> builder.relationshipId(relationship.getId()));
		return relationship;
	}

	private EntityProperties createProperties(long id) {
		return new EntityProperties(id, notifier);
	}

	private NodeLabels createLabels(long id) {
		return new NodeLabels(id, notifier);
	}

	@Override
	public Stream<Node> getAllNodes() {
		return nodeList.stream();
	}

	@Override
	public Stream<Relationship> getAllRelationships() {
		return relationshipList.stream();
	}

	@Override
	public Entity getEntityById(long id) {
		return Optional.ofNullable(entities.get(id)).orElseThrow(() -> createEntityNotFoundException(id));
	}

	@Override
	public Node getNodeById(long id) {
		return Optional.ofNullable(entities.get(id))
			.filter(instanceOf(Node.class))
			.map(Node.class::cast)
			.orElseThrow(() -> createNodeNotFoundException(id));
	}

	@Override
	public Relationship getRelationshipById(long id) {
		return Optional.ofNullable(entities.get(id))
			.filter(instanceOf(Relationship.class))
			.map(Relationship.class::cast)
			.orElseThrow(() -> createRelationshipNotFoundException(id));
	}
}
