package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.events.*;
import dev.nokee.model.registry.ModelRegistry;
import dev.nokee.model.streams.ModelStream;
import dev.nokee.model.streams.Topic;

final class DefaultModelRegistry implements ModelRegistry {
	private final GraphTopics topics = new GraphTopics();
	private final Graph graph = Graph.builder().listener(topics).build();
	private final ModelProjectionFactory projectionFactory = new DefaultModelProjectionFactory(graph);
	private final ModelNode root = new DefaultModelNodeFactory(graph).create(graph.createNode()); // TODO: Maybe we will want the root node to have the name of the target (project or settings)

	@Override
	public ModelNode getRoot() {
		return root;
	}

	@Override
	public ModelStream<ModelProjection> allProjections() {
		return ModelStream.of(topics.projectionRelationships);
	}

	private final class GraphTopics implements EventListener {
		private final Topic<ModelProjection> projectionRelationships = Topic.of(() -> graph.getAllRelationships().filter(it -> it.isType(DefaultModelNode.PROJECTION_RELATIONSHIP_TYPE)).map(it -> projectionFactory.create(it.getEndNode())));


		@Override
		public void nodeCreated(NodeCreatedEvent event) {
			// nothing to do
		}

		@Override
		public void relationshipCreated(RelationshipCreatedEvent event) {
			if (event.getRelationship().isType(DefaultModelNode.PROJECTION_RELATIONSHIP_TYPE)) {
				projectionRelationships.accept(projectionFactory.create(event.getRelationship().getEndNode()));
			}
		}

		@Override
		public void propertyChanged(PropertyChangedEvent event) {
			// nothing to do
		}

		@Override
		public void labelAdded(LabelAddedEvent event) {
			// nothing to do
		}
	}
}
