package dev.gradleplugins.documentationkit;

import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import dev.nokee.model.internal.type.TypeOf;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.annotation.Generated;
import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelActions.discover;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.core.NodeRegistration.unmanaged;
import static dev.nokee.model.internal.type.ModelType.of;

@Generated("Manually generated")
public class DslReferenceDocumentationModelElementRegistrationFactory implements NodeRegistrationFactory<DslReferenceDocumentation> {
	private final DslMetaDataModelElementRegistrationFactory dslMetaDataFactory;
	private final DslContentModelElementRegistrationFactory dslContentFactory;
	private final DslReferenceDocumentationDependenciesModelElementRegistrationFactory dependenciesFactory;
	private final ObjectFactory objects;

	@Inject
	public DslReferenceDocumentationModelElementRegistrationFactory(DslMetaDataModelElementRegistrationFactory dslMetaDataFactory, DslContentModelElementRegistrationFactory dslContentFactory, DslReferenceDocumentationDependenciesModelElementRegistrationFactory dependenciesFactory, ObjectFactory objects) {
		this.dslMetaDataFactory = dslMetaDataFactory;
		this.dslContentFactory = dslContentFactory;
		this.dependenciesFactory = dependenciesFactory;
		this.objects = objects;
	}

	@Override
	public NodeRegistration<DslReferenceDocumentation> create(String name) {
		return unmanaged(name, of(DslReferenceDocumentation.class), DslReferenceDocumentationModelElement::new)
			.action(self(discover(context -> {
				context.register(dslMetaDataFactory.create("dslMetaData"));
				context.register(dslContentFactory.create("dslContent"));
				context.register(dependenciesFactory.create("dependencies"));
				context.register(unmanaged("permalink", of(new TypeOf<Property<String>>() {}), () -> objects.property(String.class)));
				context.register(unmanaged("classDocbookDirectory", of(DirectoryProperty.class), () -> objects.directoryProperty()));
			})));
	}
}
