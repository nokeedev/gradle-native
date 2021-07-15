package dev.nokee.model.internal;

import com.google.common.base.Preconditions;
import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.core.ModelProjectionBuilderAction;
import dev.nokee.model.core.TypeAwareModelProjection;
import dev.nokee.model.graphdb.*;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Named;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static dev.nokee.model.internal.ModelSpecs.projectionOf;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class DefaultModelNode implements ModelNode {
	static final RelationshipType OWNERSHIP_RELATIONSHIP_TYPE = RelationshipType.withName("OWNS");
	static final RelationshipType PROJECTION_RELATIONSHIP_TYPE = RelationshipType.withName("PROJECTIONS");
	@EqualsAndHashCode.Exclude private final Graph graph;
	@EqualsAndHashCode.Include private final Node delegate;
	@EqualsAndHashCode.Exclude private final ModelFactory factory;

	public DefaultModelNode(ModelFactory factory, Graph graph, Node delegate) {
		this.graph = graph;
		this.delegate = delegate;
		this.factory = factory;
	}

	@Override
	public ModelNode newChildNode(Object identity) {
		Preconditions.checkArgument(!Objects.equals(identity, DomainObjectIdentities.root()), "Cannot use known root identity as child node identity.");
		Preconditions.checkArgument(!find(identity).isPresent(), "Child node with identity '%s' already exists.", identity);
		val name = nameOf(identity);
		val childNode = graph.createNode()
			.addLabel(Label.label("NODE"))
			.property("identity", identity)
			.property("name", name);
//			.property("path", getPath().child(name))
//			.property("identifier", getIdentifier().child(identity));
		delegate.createRelationshipTo(childNode, OWNERSHIP_RELATIONSHIP_TYPE);
		return factory.createNode(childNode);
	}

	private static String nameOf(Object identity) {
		if (identity instanceof Named) {
			return ((Named) identity).getName();
		} else {
			return identity.toString();
		}
	}

	@Override
	public <S> TypeAwareModelProjection<S> newProjection(ModelProjectionBuilderAction<S> builderAction) {
		val builder = (DefaultModelProjection.Builder.TypeAwareBuilder<S>) builderAction.apply(DefaultModelProjection.builder().graph(graph));
		val projection = builder.build();
		val projectionNode = projection.getDelegate();
		delegate.createRelationshipTo(projectionNode, PROJECTION_RELATIONSHIP_TYPE);
		return projection;
	}

	@Override
	public String getName() {
		return (String) delegate.getProperty("name");
	}

	@Override
	public Optional<ModelNode> getParent() {
		return delegate.getSingleRelationship(OWNERSHIP_RELATIONSHIP_TYPE, Direction.INCOMING).map(it -> factory.createNode(it.getStartNode()));
	}

	@Override
	public boolean canBeViewedAs(Class<?> type) {
		requireNonNull(type);
		return getProjections().anyMatch(projectionOf(type));
	}

	@Override
	public <T> T get(Class<T> type) {
		requireNonNull(type);
		return getProjections().filter(projectionOf(type))
			.findFirst()
			.map(it -> it.get(type))
			.orElseThrow(RuntimeException::new);
	}

	@Override
	public ModelNode get(Object identity) {
		return find(identity).orElseThrow(() -> new RuntimeException(String.format("No child node with identity '%s'.", identity)));
	}

	@Override
	public Optional<ModelNode> find(Object identity) {
		requireNonNull(identity);
		return getChildNodes().filter(it -> it.getIdentity().equals(identity)).findFirst();
	}

	@Override
	public Object getIdentity() {
		return delegate.getProperty("identity", DomainObjectIdentities.root());
	}

	@Override
	public Stream<ModelNode> getChildNodes() {
		return delegate.getRelationships(Direction.OUTGOING, OWNERSHIP_RELATIONSHIP_TYPE)
			.map(Relationship::getEndNode)
			.map(factory::createNode);
	}

	@Override
	public Stream<ModelProjection> getProjections() {
		return delegate.getRelationships(PROJECTION_RELATIONSHIP_TYPE)
			.map(Relationship::getEndNode)
			.map(factory::createProjection);
	}
}
