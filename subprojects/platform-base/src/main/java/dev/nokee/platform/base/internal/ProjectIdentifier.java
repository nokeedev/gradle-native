package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.gradle.api.Project;
import org.gradle.util.Path;

import java.util.Optional;

@ToString
@EqualsAndHashCode
public class ProjectIdentifier implements DomainObjectIdentifierInternal {
	private final Path path;

	private ProjectIdentifier(Path path) {
		this.path = path;
	}

	public String getName() {
		return path.getName();
	}

	@Override
	public Optional<? extends DomainObjectIdentifierInternal> getParentIdentifier() {
		return Optional.empty();
	}

	public static ProjectIdentifier of(String name) {
		return new ProjectIdentifier(Path.ROOT.child(name));
	}

	public static ProjectIdentifier of(Project project) {
		return new ProjectIdentifier(Path.path(project.getPath()).child(project.getName()));
	}

	@Override
	public String getDisplayName() {
		return "project '" + path.toString() + "'";
	}
}
