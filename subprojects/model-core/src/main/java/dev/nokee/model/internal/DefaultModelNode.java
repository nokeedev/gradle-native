package dev.nokee.model.internal;

import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.*;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

@EqualsAndHashCode
final class DefaultModelNode implements ModelNode {
	private static final RelationshipType OWNERSHIP_RELATIONSHIP_TYPE = RelationshipType.withName("OWNS");
	private static final RelationshipType PROJECTION_RELATIONSHIP_TYPE = RelationshipType.withName("PROJECTIONS");
	@EqualsAndHashCode.Exclude private final Graph graph;
	@EqualsAndHashCode.Include private final Node delegate;

	public DefaultModelNode(Graph graph, Node delegate) {
		this.graph = graph;
		this.delegate = delegate;
	}

	@Override
	public ModelNode newChildNode(Object identity) {
		val name = nameOf(identity);
		val childNode = graph.createNode()
			.property("identity", identity)
			.property("name", name);
//			.property("path", getPath().child(name))
//			.property("identifier", getIdentifier().child(identity));
		delegate.createRelationshipTo(childNode, OWNERSHIP_RELATIONSHIP_TYPE);
		return new DefaultModelNode(graph, childNode);
	}

	private static String nameOf(Object identity) {
		if (identity instanceof Named) {
			return ((Named) identity).getName();
		} else {
			return identity.toString();
		}
	}

	@Override
	public ModelProjection newProjection(Consumer<? super ModelProjection.Builder> builderAction) {
		val builder = ProjectionSpec.builder();
		builderAction.accept(new ModelProjection.Builder() {
			@Override
			public ModelProjection.Builder type(Class<?> type) {
				builder.type(type);
				return this;
			}

			@Override
			public ModelProjection.Builder forProvider(NamedDomainObjectProvider<?> domainObjectProvider) {
				builder.forProvider(domainObjectProvider);
				return this;
			}

			@Override
			public ModelProjection.Builder forInstance(Object instance) {
				builder.forInstance(instance);
				return this;
			}
		});
		val projectionNode = graph.createNode().property("spec", builder.build());
//			.property("type", type)
//			.property("identifier", getIdentifier());
		delegate.createRelationshipTo(projectionNode, PROJECTION_RELATIONSHIP_TYPE);
		return new DefaultModelProjection(graph, projectionNode);
	}

	@Override
	public String getName() {
		return (String) delegate.getProperty("name");
	}

	@Override
	public Optional<ModelNode> getParent() {
		return delegate.getSingleRelationship(OWNERSHIP_RELATIONSHIP_TYPE, Direction.INCOMING).map(it -> new DefaultModelNode(graph, it.getStartNode()));
	}

	@Override
	public boolean canBeViewedAs(Class<?> type) {
		return getProjections().anyMatch(it -> it.canBeViewedAs(type));
	}

	@Override
	public <T> T get(Class<T> type) {
		return getProjections().filter(it -> it.canBeViewedAs(type))
			.findFirst()
			.map(it -> it.get(type))
			.orElseThrow(RuntimeException::new);
	}

	@Override
	public ModelNode get(Object identity) {
		return find(identity).orElseThrow(RuntimeException::new);
	}

	@Override
	public Optional<ModelNode> find(Object identity) {
		return getChildNodes().filter(it -> it.getIdentity().equals(identity)).findFirst();
	}

	@Override
	public Object getIdentity() {
		return delegate.getProperty("identity", ""); // TODO: Is default value enough for root
	}

	@Override
	public Stream<ModelNode> getChildNodes() {
		return delegate.getRelationships(Direction.OUTGOING, OWNERSHIP_RELATIONSHIP_TYPE)
			.map(Relationship::getEndNode)
			.map(it -> new DefaultModelNode(graph, it));
	}

	@Override
	public Stream<ModelProjection> getProjections() {
		return delegate.getRelationships(PROJECTION_RELATIONSHIP_TYPE)
			.map(Relationship::getEndNode)
			.map(it -> new DefaultModelProjection(graph, it));
	}
}
