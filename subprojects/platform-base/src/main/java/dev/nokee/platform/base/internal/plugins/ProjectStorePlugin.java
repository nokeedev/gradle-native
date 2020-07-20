package dev.nokee.platform.base.internal.plugins;

import dev.nokee.platform.base.internal.DefaultDomainObjectStore;
import dev.nokee.platform.base.internal.DomainObjectStore;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class ProjectStorePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		val store = getObjects().newInstance(DefaultDomainObjectStore.class);
		project.getExtensions().add(DomainObjectStore.class, "store", store);
	}

	@Inject
	protected abstract ObjectFactory getObjects();
}
