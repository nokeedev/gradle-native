package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import dev.nokee.ide.visualstudio.VisualStudioIdePropertyGroup;
import dev.nokee.ide.visualstudio.VisualStudioIdeTarget;
import lombok.Getter;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;

import static dev.nokee.utils.ConfigureUtils.configureDisplayName;

public final class DefaultVisualStudioIdeTarget implements VisualStudioIdeTarget, Named {
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

	@Override
	public String getName() {
		return VisualStudioIdeUtils.asName(projectConfiguration);
	}

	private VisualStudioIdePropertyGroup newPropertyGroup(String name) {
		return objectFactory.newInstance(NamedVisualStudioIdePropertyGroup.class, name);
	}
}
