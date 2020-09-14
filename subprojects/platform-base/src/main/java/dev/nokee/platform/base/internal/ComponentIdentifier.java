package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.val;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@ToString
@EqualsAndHashCode
public final class ComponentIdentifier<T extends Component> implements DomainObjectIdentifierInternal {
	private static final ComponentName MAIN_COMPONENT_NAME = ComponentName.of("main");
	private static final String MAIN_COMPONENT_DEFAULT_DISPLAY_NAME = "main component";
	@Getter private final ComponentName name;
	@Getter private final Class<T> type;
	@Getter private final String displayName;
	@Getter private final ProjectIdentifier projectIdentifier;

	private ComponentIdentifier(ComponentName name, Class<T> type, String displayName, ProjectIdentifier projectIdentifier) {
		this.name = requireNonNull(name);
		this.type = requireNonNull(type);
		this.displayName = requireNonNull(displayName);
		this.projectIdentifier = requireNonNull(projectIdentifier);
	}

	public static <T extends Component> ComponentIdentifier<T> ofMain(Class<T> type, ProjectIdentifier projectIdentifier) {
		return new ComponentIdentifier<>(MAIN_COMPONENT_NAME, type, MAIN_COMPONENT_DEFAULT_DISPLAY_NAME, projectIdentifier);
	}

	public static <T extends Component> ComponentIdentifier<T> of(ComponentName name, Class<T> type, ProjectIdentifier projectIdentifier) {
		if (MAIN_COMPONENT_NAME.equals(name)) {
			return ofMain(type, projectIdentifier);
		}
		return new ComponentIdentifier<>(name, type, displayNameOf(name.get()), projectIdentifier);
	}

	@Override
	public Optional<ProjectIdentifier> getParentIdentifier() {
		return Optional.of(projectIdentifier);
	}

	public boolean isMainComponent() {
		return name.equals(MAIN_COMPONENT_NAME);
	}

	public static Builder<Component> builder() {
		return new Builder<>();
	}

    public static final class Builder<T extends Component> {
		private ComponentName name;
		private Class<? extends T> type;
		private String displayName;
		private ProjectIdentifier projectIdentifier;

		public Builder<T> withName(ComponentName name) {
			this.name = name;
			return this;
		}

		@SuppressWarnings("unchecked")
		public <S extends T> Builder<S> withType(Class<S> type) {
			this.type = type;
			return (Builder<S>) this;
		}

		public Builder<T> withDisplayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public Builder<T> withProjectIdentifier(ProjectIdentifier projectIdentifier) {
			this.projectIdentifier = projectIdentifier;
			return this;
		}

		public ComponentIdentifier<T> build() {
			@SuppressWarnings("unchecked")
			val componentType = (Class<T>) type;
			return new ComponentIdentifier<>(name, componentType, displayName(), projectIdentifier);
		}

		private String displayName() {
			return Optional.ofNullable(displayName).orElseGet(() -> defaultDisplayNameFor(name));
		}
	}

	private static String defaultDisplayNameFor(ComponentName componentName) {
		if (MAIN_COMPONENT_NAME.equals(componentName)) {
			return MAIN_COMPONENT_DEFAULT_DISPLAY_NAME;
		}
		return displayNameOf(componentName.get());
	}

	private static String displayNameOf(String componentName) {
		return "component '" + componentName + "'";
	}
}
