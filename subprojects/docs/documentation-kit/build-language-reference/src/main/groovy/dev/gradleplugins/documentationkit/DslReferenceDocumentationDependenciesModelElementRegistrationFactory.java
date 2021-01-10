package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Generated;
import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public final class DslReferenceDocumentationDependenciesModelElementRegistrationFactory implements NodeRegistrationFactory<DslReferenceDocumentation.Dependencies> {
	private final ObjectFactory objects;
	private final ResolvableDependencyBucketRegistrationFactory resolvableFactory;
	private final ConsumableDependencyBucketRegistrationFactory consumableFactory;

	@Inject
	public DslReferenceDocumentationDependenciesModelElementRegistrationFactory(ObjectFactory objects, ResolvableDependencyBucketRegistrationFactory resolvableFactory, ConsumableDependencyBucketRegistrationFactory consumableFactory) {
		this.objects = objects;
		this.resolvableFactory = resolvableFactory;
		this.consumableFactory = consumableFactory;
	}

	@Override
	public NodeRegistration<DslReferenceDocumentation.Dependencies> create(String name) {
		return NodeRegistration.unmanaged(name, of(DslReferenceDocumentation.Dependencies.class), () -> new DslReferenceDocumentationDependenciesModelElement(objects))
			.action(self(discover(context -> {
				context.register(resolvableFactory.create("dslMetaData"));
				context.register(consumableFactory.create("dslMetaDataElements"));
			})));
	}
}
