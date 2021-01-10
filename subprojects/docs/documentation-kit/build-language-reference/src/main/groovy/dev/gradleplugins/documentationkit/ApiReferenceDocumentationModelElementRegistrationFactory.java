package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;

import javax.annotation.Generated;
import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.core.NodeRegistration.unmanaged;
import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public final class ApiReferenceDocumentationModelElementRegistrationFactory implements NodeRegistrationFactory<ApiReferenceDocumentation> {
	private final ApiReferenceManifestModelElementRegistrationFactory manifestFactory;
	private final ApiReferenceDocumentationDependenciesModelElementRegistrationFactory dependenciesFactory;

	@Inject
	public ApiReferenceDocumentationModelElementRegistrationFactory(ApiReferenceManifestModelElementRegistrationFactory manifestFactory, ApiReferenceDocumentationDependenciesModelElementRegistrationFactory dependenciesFactory) {
		this.manifestFactory = manifestFactory;
		this.dependenciesFactory = dependenciesFactory;
	}

	@Override
	public NodeRegistration<ApiReferenceDocumentation> create(String name) {
		return unmanaged(name, of(ApiReferenceDocumentation.class), ApiReferenceDocumentationModelElement::new)
			.action(self(discover(context -> {
				context.register(manifestFactory.create("manifest"));
				context.register(dependenciesFactory.create("dependencies"));
			})));
	}
}
