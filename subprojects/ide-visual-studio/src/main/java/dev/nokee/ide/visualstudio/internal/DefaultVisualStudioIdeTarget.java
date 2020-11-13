package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import dev.nokee.ide.visualstudio.VisualStudioIdePropertyGroup;
import dev.nokee.ide.visualstudio.VisualStudioIdeTarget;
import dev.nokee.utils.ConfigureUtils;
import lombok.Getter;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.tasks.TaskDependencyContainer;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

public final class DefaultVisualStudioIdeTarget implements VisualStudioIdeTarget, Named, TaskDependencyContainer {
	private final ObjectFactory objectFactory;
	@Getter private final VisualStudioIdeProjectConfiguration projectConfiguration;
	@Getter private final DefaultVisualStudioIdePropertyGroup properties;
	@Getter private final NamedDomainObjectContainer<VisualStudioIdePropertyGroup> itemProperties;
	@Getter private final RegularFileProperty productLocation;

	public DefaultVisualStudioIdeTarget(VisualStudioIdeProjectConfiguration projectConfiguration, ObjectFactory objectFactory) {
		this.projectConfiguration = projectConfiguration;
		this.objectFactory = objectFactory;
		this.productLocation = configureDisplayName(objectFactory.fileProperty(), "productLocation");
		this.properties = objectFactory.newInstance(DefaultVisualStudioIdePropertyGroup.class);
		this.itemProperties = objectFactory.domainObjectContainer(VisualStudioIdePropertyGroup.class, this::newPropertyGroup);
	}

	public void setProductLocation(Object value) {
		ConfigureUtils.setPropertyValue(productLocation, value);
	}

	@Override
	public String getName() {
		return VisualStudioIdeUtils.asName(projectConfiguration);
	}

	private VisualStudioIdePropertyGroup newPropertyGroup(String name) {
		return objectFactory.newInstance(NamedVisualStudioIdePropertyGroup.class, name);
	}

	@Override
	public void visitDependencies(TaskDependencyResolveContext context) {
		properties.visitDependencies(context);
		itemProperties.stream().map(DefaultVisualStudioIdePropertyGroup.class::cast).forEach(it -> it.visitDependencies(context));
	}
}
