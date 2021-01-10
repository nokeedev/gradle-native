package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;

import javax.annotation.Generated;
import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public final class ApiReferenceDocumentationDependenciesModelElementRegistrationFactory implements NodeRegistrationFactory<ApiReferenceDocumentation.Dependencies> {
	private final ConsumableDependencyBucketRegistrationFactory consumableFactory;
	private final ResolvableDependencyBucketRegistrationFactory resolvableFactory;
	private final DeclarableDependencyBucketRegistrationFactory declarableFactory;

	@Inject
	public ApiReferenceDocumentationDependenciesModelElementRegistrationFactory(ConsumableDependencyBucketRegistrationFactory consumableFactory, ResolvableDependencyBucketRegistrationFactory resolvableFactory, DeclarableDependencyBucketRegistrationFactory declarableFactory) {
		this.consumableFactory = consumableFactory;
		this.resolvableFactory = resolvableFactory;
		this.declarableFactory = declarableFactory;
	}

	@Override
	public NodeRegistration<ApiReferenceDocumentation.Dependencies> create(String name) {
		return NodeRegistration.unmanaged(name, of(ApiReferenceDocumentation.Dependencies.class), ApiReferenceDocumentationDependenciesModelElement::new)
			.action(self(discover(context -> {
				context.register(declarableFactory.create("api"));
				context.register(resolvableFactory.create("manifest"));
				context.register(consumableFactory.create("manifestElements"));
			})));
	}
}
