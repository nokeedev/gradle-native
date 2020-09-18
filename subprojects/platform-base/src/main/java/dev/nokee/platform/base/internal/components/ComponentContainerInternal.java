package dev.nokee.platform.base.internal.components;

import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import org.gradle.api.provider.Provider;

import java.util.Set;

public interface ComponentContainerInternal extends ComponentContainer {
	ComponentContainerInternal disallowChanges();

	Provider<Set<? extends Component>> getElements();
}
