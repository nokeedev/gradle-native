package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import lombok.Value;
import org.gradle.api.Project;

import java.util.Optional;

@Value
public class ProjectIdentifier implements DomainObjectIdentifierInternal {
	String name;

	@Override
	public Optional<? extends DomainObjectIdentifierInternal> getParentIdentifier() {
		return Optional.empty();
	}

	public static ProjectIdentifier of(Project project) {
		return new ProjectIdentifier(project.getName());
	}
}
