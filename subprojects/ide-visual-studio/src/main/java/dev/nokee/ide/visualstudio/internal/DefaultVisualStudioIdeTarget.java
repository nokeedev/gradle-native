package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import dev.nokee.ide.visualstudio.VisualStudioIdePropertyGroup;
import dev.nokee.ide.visualstudio.VisualStudioIdeTarget;
import lombok.Getter;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class DefaultVisualStudioIdeTarget implements VisualStudioIdeTarget, Named {
	@Getter private final VisualStudioIdeProjectConfiguration projectConfiguration;
	@Getter private final DefaultVisualStudioIdePropertyGroup properties;
	@Getter private final NamedDomainObjectContainer<VisualStudioIdePropertyGroup> itemProperties;

	@Inject
	public DefaultVisualStudioIdeTarget(VisualStudioIdeProjectConfiguration projectConfiguration) {
		this.projectConfiguration = projectConfiguration;
		this.properties = getObjects().newInstance(DefaultVisualStudioIdePropertyGroup.class);
		this.itemProperties = getObjects().domainObjectContainer(VisualStudioIdePropertyGroup.class, this::newPropertyGroup);
	}

	@Override
	public String getName() {
		return VisualStudioIdeUtils.asName(projectConfiguration);
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	private VisualStudioIdePropertyGroup newPropertyGroup(String name) {
		return getObjects().newInstance(NamedVisualStudioIdePropertyGroup.class, name);
	}
}
