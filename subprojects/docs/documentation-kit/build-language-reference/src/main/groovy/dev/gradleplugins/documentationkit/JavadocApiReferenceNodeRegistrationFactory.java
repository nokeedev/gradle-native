package dev.gradleplugins.documentationkit;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import dev.nokee.model.internal.type.TypeOf;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import javax.annotation.Generated;
import javax.inject.Inject;
import java.net.URI;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.core.NodeRegistration.unmanaged;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

@Generated("Manually generated")
public final class JavadocApiReferenceNodeRegistrationFactory implements NodeRegistrationFactory<JavadocApiReference> {
	private final ObjectFactory objects;

	@Inject
	public JavadocApiReferenceNodeRegistrationFactory(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public NodeRegistration<JavadocApiReference> create(String name) {
		return NodeRegistration.unmanaged(name, of(JavadocApiReference.class), JavadocApiReferenceModelElement::new)
			.action(self(discover(context -> {
				context.register(sourceSet("sources", LanguageSourceSet.class));
				context.register(unmanaged("permalink", of(new TypeOf<Property<String>>() {}), () -> configureDisplayName(objects.property(String.class), "permalink")));
				context.register(unmanaged("title", of(new TypeOf<Property<String>>() {}), () -> configureDisplayName(objects.property(String.class), "title")));
				context.register(unmanaged("destinationDirectory", of(DirectoryProperty.class), () -> configureDisplayName(objects.directoryProperty(), "destinationDirectory")));
				context.register(unmanaged("links", of(new TypeOf<SetProperty<URI>>() {}), () -> configureDisplayName(objects.setProperty(URI.class), "links")));
				context.register(unmanaged("classpath", of(ConfigurableFileCollection.class), () -> objects.fileCollection()));
			})));
	}
}
