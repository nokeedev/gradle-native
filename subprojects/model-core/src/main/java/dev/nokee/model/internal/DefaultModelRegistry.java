package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.core.ModelRule;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.graphdb.events.*;
import dev.nokee.model.registry.ModelRegistry;
import dev.nokee.model.streams.ModelStream;
import dev.nokee.model.streams.Topic;
import org.gradle.api.model.ObjectFactory;

final class DefaultModelRegistry implements ModelRegistry {
	private final GraphTopics topics = new GraphTopics();
	private final Graph graph = Graph.builder().listener(topics).build();
	private final ModelFactory factory;
	private final ModelNode root;
	private final ModelStream<ModelProjection> allProjections = ModelStream.of(topics.projectionRelationships);
	private final ObjectFactory objects;

	DefaultModelRegistry(ObjectFactory objects) {
		this(objects, new DefaultNamedDomainObjectRegistry());
	}

	DefaultModelRegistry(ObjectFactory objects, NamedDomainObjectRegistry registry) {
		this.objects = objects;
		this.factory = new DefaultModelFactory(graph, objects, registry);
		this.root = factory.createNode(graph.createNode()); // TODO: Maybe we will want the root node to have the name of the target (project or settings)
	}

	@Override
	public ModelNode getRoot() {
		return root;
	}

	@Override
	public ModelStream<ModelProjection> allProjections() {
		return allProjections;
	}

	@Override
	public void registerRule(ModelRule rule) {
		rule.execute(allProjections);
	}

	@Override
	public void registerRule(Class<? extends ModelRule> rule) {
		objects.newInstance(rule).execute(allProjections);
	}

	private final class GraphTopics implements EventListener {
		private final Topic<ModelProjection> projectionRelationships = Topic.of(() -> graph.getAllRelationships().filter(it -> it.isType(DefaultModelNode.PROJECTION_RELATIONSHIP_TYPE)).map(it -> factory.createProjection(it.getEndNode())));


		@Override
		public void nodeCreated(NodeCreatedEvent event) {
			// nothing to do
		}

		@Override
		public void relationshipCreated(RelationshipCreatedEvent event) {
			if (event.getRelationship().isType(DefaultModelNode.PROJECTION_RELATIONSHIP_TYPE)) {
				projectionRelationships.accept(factory.createProjection(event.getRelationship().getEndNode()));
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
