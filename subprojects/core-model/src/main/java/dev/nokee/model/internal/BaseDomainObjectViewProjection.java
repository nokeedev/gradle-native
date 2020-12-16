package dev.nokee.model.internal;

import com.google.common.collect.ImmutableSet;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.Set;

import static dev.nokee.model.internal.core.ModelActions.*;
import static dev.nokee.model.internal.core.ModelNodeContext.getCurrentModelNode;
import static dev.nokee.model.internal.core.ModelNodes.*;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;

public class BaseDomainObjectViewProjection implements AbstractModelNodeBackedDomainObjectView.Projection {
	private final ProviderFactory providerFactory;
	private final ObjectFactory objectFactory;
	private final ModelNode node = getCurrentModelNode();

	@Inject
	public BaseDomainObjectViewProjection(ProviderFactory providerFactory, ObjectFactory objectFactory) {
		this.providerFactory = providerFactory;
		this.objectFactory = objectFactory;
	}

	@Override
	public <T> void configureEach(ModelType<T> type, Action<? super T> action) {
		node.applyTo(allDirectDescendants(stateAtLeast(ModelNode.State.Realized).and(withType(type))), executeUsingProjection(type, action));
	}

	@Override
	public <T> void configureEach(ModelType<T> type, Spec<? super T> spec, Action<? super T> action) {
		node.applyTo(allDirectDescendants(stateAtLeast(ModelNode.State.Realized).and(withType(type)).and(isSatisfiedByProjection(type, spec))), executeUsingProjection(type, action));
	}

	private <T> Set<T> get(ModelType<T> type) {
		return node.getDirectDescendants()
			.stream()
			.filter(it -> it.canBeViewedAs(type))
			.map(it -> it.realize().get(type))
			.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public <T> Provider<Set<T>> getElements(ModelType<T> type) {
		return providerFactory.provider(() -> get(type));
	}

	@Override
	public <T> void whenElementKnown(ModelType<T> type, Action<? super KnownDomainObject<T>> action) {
		node.applyTo(allDirectDescendants(stateAtLeast(ModelNode.State.Registered).and(withType(type))), once(executeAsKnownProjection(type, action)));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> DomainObjectView<T> createSubView(ModelType<T> type) {
		return (DomainObjectView<T>) objectFactory.newInstance(SubView.class, type, node);
	}

	static class SubView<T> extends BaseDomainObjectView<T> {
		@Inject
		public SubView(ModelType<T> elementType, ModelNode node) {
			super(elementType, node);
		}
	}
}
