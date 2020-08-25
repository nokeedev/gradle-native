package dev.nokee.platform.base.internal;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.util.Path;

import java.util.Optional;

@Value
public class GlobalDomainObjectIdentity implements DomainObjectIdentity {
	GlobalDomainObjectIdentity parent;
	String projectPath;
	String name;

	public GlobalDomainObjectIdentity(String projectPath, String name) {
		this(projectPath, null, name);
	}

	private GlobalDomainObjectIdentity(String projectPath, GlobalDomainObjectIdentity parent, String name) {
		this.projectPath = projectPath;
		this.name = name;
		this.parent = parent;
	}

	public GlobalDomainObjectIdentity child(String name) {
		return new GlobalDomainObjectIdentity(projectPath, this, name);
	}

	public Optional<GlobalDomainObjectIdentity> getParent() {
		return Optional.ofNullable(parent);
	}

	public Path getPath() {
		return Path.path(getQualifiedPath());
	}

	private String getQualifiedPath() {
		return parent == null ? name : parent.getQualifiedPath() + Project.PATH_SEPARATOR + name;
	}

	public String getProjectScopedName() {
		return parent == null ? name : parent.getProjectScopedName() + StringUtils.capitalize(name);
	}

	@Override
	public String toString() {
		return getQualifiedPath();
	}
}
