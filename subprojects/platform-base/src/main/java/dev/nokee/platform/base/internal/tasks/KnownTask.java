package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.internal.AbstractKnownDomainObject;
import lombok.ToString;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;

@ToString
public final class KnownTask<T extends Task> extends AbstractKnownDomainObject<Task, T> {
	KnownTask(TaskIdentifier<T> identifier, Provider<T> provider, TaskConfigurer configurer) {
		super(identifier, provider, configurer);
	}
}
