package dev.nokee.platform.base.internal.plugins;

import dev.nokee.platform.base.internal.DefaultDomainObjectStore;
import dev.nokee.platform.base.internal.DomainObjectStore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class ProjectStorePlugin implements Plugin<Project> {
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	@Inject
	public ProjectStorePlugin(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void apply(Project project) {
		val store = getObjects().newInstance(DefaultDomainObjectStore.class);
		project.getExtensions().add(DomainObjectStore.class, "store", store);

		project.afterEvaluate(proj -> store.disallowChanges());
	}
}
