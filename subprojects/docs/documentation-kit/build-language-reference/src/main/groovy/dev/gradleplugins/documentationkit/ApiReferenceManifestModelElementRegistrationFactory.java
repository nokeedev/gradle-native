package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import dev.nokee.model.internal.type.TypeOf;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;

import javax.annotation.Generated;
import javax.inject.Inject;
import java.net.URI;

import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.core.NodeRegistration.unmanaged;
import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public final class ApiReferenceManifestModelElementRegistrationFactory implements NodeRegistrationFactory<ApiReferenceManifest> {
	private final ObjectFactory objects;

	@Inject
	public ApiReferenceManifestModelElementRegistrationFactory(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public NodeRegistration<ApiReferenceManifest> create(String name) {
		return unmanaged(name, of(ApiReferenceManifest.class), ApiReferenceManifestModelElement::new)
			.action(self(discover(context -> {
				context.register(unmanaged("sources", of(ConfigurableFileCollection.class), () -> objects.fileCollection()));
				context.register(unmanaged("dependencies", of(new TypeOf<SetProperty<Dependency>>() {}), () -> objects.setProperty(Dependency.class)));
				context.register(unmanaged("repositories", of(new TypeOf<SetProperty<URI>>() {}), () -> objects.setProperty(URI.class)));
				context.register(unmanaged("destinationLocation", of(DirectoryProperty.class), () -> objects.directoryProperty()));
			})));
	}
}
