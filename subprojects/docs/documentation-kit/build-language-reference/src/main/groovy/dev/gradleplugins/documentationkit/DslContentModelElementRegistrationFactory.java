package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import dev.nokee.model.internal.type.TypeOf;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import javax.annotation.Generated;
import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.core.NodeRegistration.unmanaged;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

@Generated("Manually generated")
public final class DslContentModelElementRegistrationFactory implements NodeRegistrationFactory<DslContent> {
	private final ObjectFactory objects;

	@Inject
	public DslContentModelElementRegistrationFactory(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public NodeRegistration<DslContent> create(String name) {
		return unmanaged(name, of(DslContent.class), DslContentModelElement::new)
			.action(self(discover(context -> {
				context.register(unmanaged("classDocbookDirectories", of(ConfigurableFileCollection.class), () -> objects.fileCollection()));
				context.register(unmanaged("classMetaDataFiles", of(ConfigurableFileCollection.class), () -> objects.fileCollection()));
				context.register(unmanaged("templateFile", of(RegularFileProperty.class), () -> configureDisplayName(objects.fileProperty(), "templateFile")));
				context.register(unmanaged("classNames", of(new TypeOf<SetProperty<String>>() {}), () -> configureDisplayName(objects.setProperty(String.class), "classNames")));
				context.register(unmanaged("permalink", of(new TypeOf<Property<String>>() {}), () -> configureDisplayName(objects.property(String.class), "permalink")));
				context.register(unmanaged("contentDirectory", of(DirectoryProperty.class), () -> configureDisplayName(objects.directoryProperty(), "contentDirectory")));
			})));
	}
}
