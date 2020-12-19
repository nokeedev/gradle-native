package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import org.gradle.api.Project;

public interface ComponentEntityFixture extends NokeeEntitiesFixture {
	static ComponentIdentifier<Component> mainComponentIdentifier() {
		return ComponentIdentifier.ofMain(Component.class, ProjectIdentifier.of("root"));
	}

	static ComponentIdentifier<Component> aComponentIdentifier(String name) {
		return ComponentIdentifier.of(ComponentName.of(name), Component.class, ProjectIdentifier.of("root"));
	}

	Project getProject();
}
