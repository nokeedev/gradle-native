package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.components.ComponentConfigurer;
import dev.nokee.platform.base.internal.components.ComponentRepository;
import dev.nokee.platform.base.internal.components.KnownComponent;
import dev.nokee.platform.base.internal.components.KnownComponentFactory;
import org.gradle.api.Project;

public interface ComponentEntityFixture extends NokeeEntitiesFixture {
	static ComponentIdentifier<Component> mainComponentIdentifier() {
		return ComponentIdentifier.ofMain(Component.class, ProjectIdentifier.of("root"));
	}

	static ComponentIdentifier<Component> aComponentIdentifier(String name) {
		return ComponentIdentifier.of(ComponentName.of(name), Component.class, ProjectIdentifier.of("root"));
	}

	Project getProject();

	default ComponentConfigurer getComponentConfigurer() {
		return new ComponentConfigurer(getEventPublisher());
	}

	default ComponentRepository getComponentRepository() {
		return new ComponentRepository(getEventPublisher(), getEntityRealizer(), getProject().getProviders());
	}

	default KnownComponentFactory getKnownComponentFactory() {
		return new KnownComponentFactory(this::getComponentRepository, this::getComponentConfigurer);
	}

	default <S extends Component> KnownComponent<S> known(ComponentIdentifier<S> identifier) {
		return getKnownComponentFactory().create(identifier);
	}
}
