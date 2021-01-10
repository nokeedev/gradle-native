package dev.gradleplugins.documentationkit;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Generated;
import javax.inject.Inject;

import static dev.nokee.language.base.internal.plugins.LanguageBasePlugin.sourceSet;
import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.core.NodeRegistration.unmanaged;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

@Generated("Manually generated")
public final class DslMetaDataModelElementRegistrationFactory implements NodeRegistrationFactory<DslMetaData> {
	private final ObjectFactory objects;

	@Inject
	public DslMetaDataModelElementRegistrationFactory(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public NodeRegistration<DslMetaData> create(String name) {
		return unmanaged(name, of(DslMetaData.class), DslMetaDataModelElement::new)
			.action(self(discover(context -> {
				context.register(sourceSet("sources", LanguageSourceSet.class));
				context.register(unmanaged("classDocbookFiles", of(ConfigurableFileCollection.class), () -> objects.fileCollection()));
				context.register(unmanaged("extractedMetaDataFile", of(DirectoryProperty.class), () -> configureDisplayName(objects.directoryProperty(), "extractedMetaDataFile")));
			})));
	}
}
