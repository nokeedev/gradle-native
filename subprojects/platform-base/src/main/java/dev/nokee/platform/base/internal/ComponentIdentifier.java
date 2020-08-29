package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import lombok.*;

import java.util.Optional;

@Value
public class ComponentIdentifier implements DomainObjectIdentifierInternal {
	private static final String MAIN_COMPONENT_NAME = "main";
	private static final String MAIN_COMPONENT_DEFAULT_DISPLAY_NAME = "main component";
	String name;
	String displayName;
	ProjectIdentifier projectIdentifier;

	public ComponentIdentifier(String name, ProjectIdentifier projectIdentifier) {
		this(name, defaultDisplayNameFor(name), projectIdentifier);
	}

	public ComponentIdentifier(String name, String displayName, ProjectIdentifier projectIdentifier) {
		Preconditions.checkArgument(name != null, "Cannot construct a component identifier because name is null.");
		Preconditions.checkArgument(!name.isEmpty(), "Cannot construct a component identifier because name is invalid.");
		Preconditions.checkArgument(projectIdentifier != null, "Cannot construct a component identifier because project identifier is null.");
		this.name = name;
		this.displayName = displayName;
		this.projectIdentifier = projectIdentifier;
	}

	public static ComponentIdentifier ofMain(ProjectIdentifier projectIdentifier) {
		return new ComponentIdentifier(MAIN_COMPONENT_NAME, MAIN_COMPONENT_DEFAULT_DISPLAY_NAME, projectIdentifier);
	}

	public static ComponentIdentifier of(String name, ProjectIdentifier projectIdentifier) {
		if (MAIN_COMPONENT_NAME.equals(name)) {
			return ofMain(projectIdentifier);
		}
		return new ComponentIdentifier(name, displayNameOf(name), projectIdentifier);
	}

	@Override
	public Optional<ProjectIdentifier> getParentIdentifier() {
		return Optional.of(projectIdentifier);
	}

	public boolean isMainComponent() {
		return name.equals(MAIN_COMPONENT_NAME);
	}

	public static Builder builder() {
		return new Builder();
	}

    boolean hasCustomDisplayName() {
		return !defaultDisplayNameFor(name).equals(displayName);
    }

    public static final class Builder {
		private String name;
		private String displayName;
		private ProjectIdentifier projectIdentifier;

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public Builder withDisplayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public Builder withProjectIdentifier(ProjectIdentifier projectIdentifier) {
			this.projectIdentifier = projectIdentifier;
			return this;
		}

		public ComponentIdentifier build() {
			return new ComponentIdentifier(name, displayName(), projectIdentifier);
		}

		private String displayName() {
			return Optional.ofNullable(displayName).orElseGet(() -> defaultDisplayNameFor(name));
		}
	}

	private static String defaultDisplayNameFor(String componentName) {
		if (MAIN_COMPONENT_NAME.equals(componentName)) {
			return MAIN_COMPONENT_DEFAULT_DISPLAY_NAME;
		}
		return displayNameOf(componentName);
	}

	private static String displayNameOf(String componentName) {
		return "component '" + componentName + "'";
	}
}
