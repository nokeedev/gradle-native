package dev.nokee.platform.base.internal.tasks;

import dagger.Module;
import dagger.Provides;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Singleton;

@Module
public interface ComponentTasksModule {
	@Provides
	static ComponentTasksInternal theComponentTasks(DomainObjectIdentifierInternal identifier, TaskContainer taskContainer, KnownTaskIdentifierRegistry identifierRegistry) {
		return new ComponentTasksAdapter(identifier, taskContainer, identifierRegistry);
	}

	@Provides
	@Singleton
	static KnownTaskIdentifierRegistry theKnownTaskIdentifierRegistry() {
		return new KnownTaskIdentifierRegistryImpl();
	}
}
