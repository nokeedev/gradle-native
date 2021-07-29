package dev.nokee.model.internal;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import dev.nokee.model.core.ModelNode;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.core.ModelProjectionBuilderAction;
import dev.nokee.model.core.TypeAwareModelProjection;
import dev.nokee.model.graphdb.*;
import dev.nokee.utils.ProviderUtils;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dev.nokee.model.internal.ModelSpecs.projectionOf;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class DefaultModelNode implements ModelNode {
	static final RelationshipType OWNERSHIP_RELATIONSHIP_TYPE = RelationshipType.withName("OWNS");
	static final RelationshipType PROJECTION_RELATIONSHIP_TYPE = RelationshipType.withName("PROJECTIONS");
	@EqualsAndHashCode.Exclude private final Graph graph;
	@EqualsAndHashCode.Include private final Node delegate;
	@EqualsAndHashCode.Exclude private final ModelFactory modelFactory;
	@EqualsAndHashCode.Exclude private final ObjectFactory objectFactory;
	@EqualsAndHashCode.Exclude private Supplier<Optional<ModelNode>> parent = Suppliers.memoize(this::computeParentNode);
	@EqualsAndHashCode.Exclude private final NamedDomainObjectRegistry registry;

	public DefaultModelNode(ModelFactory modelFactory, @Nullable ObjectFactory objectFactory, Graph graph, Node delegate, @Nullable NamedDomainObjectRegistry registry) {
		this.graph = graph;
		this.delegate = delegate;
		this.modelFactory = modelFactory;
		this.objectFactory = objectFactory;
		this.registry = registry;
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
		return modelFactory.createNode(childNode);
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
		val builder = (DefaultModelProjection.Builder.TypeAwareBuilder<S>) builderAction.apply(DefaultModelProjection.builder().registry(registry).modelFactory(modelFactory).objectFactory(objectFactory).graph(graph).owner(this));
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
		return parent.get();
	}

	private Optional<ModelNode> computeParentNode() {
		return delegate.getSingleRelationship(OWNERSHIP_RELATIONSHIP_TYPE, Direction.INCOMING)
			.map(it -> modelFactory.createNode(it.getStartNode()));
	}

	@Override
	public boolean canBeViewedAs(Class<?> type) {
		requireNonNull(type);
		return getProjections().anyMatch(projectionOf(type));
	}

	@Override
	public <T> T get(Class<T> type) {
		requireNonNull(type);
		return getProjections().filter(projectionOf(type)) // TODO: Wrong, doesn't account for provider
			.findFirst()
			.map(it -> it.get(type))
			.orElseThrow(() -> new RuntimeException(String.format("No projection of '%s' found.", type.getCanonicalName())));
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
			.map(modelFactory::createNode);
	}

	@Override
	public Stream<ModelProjection> getProjections() {
		return delegate.getRelationships(PROJECTION_RELATIONSHIP_TYPE)
			.map(Relationship::getEndNode)
			.map(modelFactory::createProjection);
	}

	@Override
	public <T> Provider<T> as(Class<T> type) {
		requireNonNull(type);
		return getProjections().filter(projectionOf(type)).findFirst().map(it -> it.as(type)).orElseGet(ProviderUtils::notDefined);
	}

	@Override
	public <T> void whenRealized(Class<T> type, Action<? super T> action) {
		requireNonNull(type);
		requireNonNull(action);
		getProjections().filter(projectionOf(type)).findFirst().orElseThrow(RuntimeException::new).whenRealized(type, action);
	}
}
