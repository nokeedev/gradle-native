package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.internal.VariantAwareComponentInternal;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.internal.tasks.ObjectsLifecycleTask;
import org.gradle.api.Action;

import static dev.nokee.utils.TaskUtils.configureDependsOn;

public class CreateVariantAwareComponentObjectsLifecycleTaskRule implements Action<VariantAwareComponentInternal<?>> {
	private final TaskRegistry taskRegistry;

	public CreateVariantAwareComponentObjectsLifecycleTaskRule(TaskRegistry taskRegistry) {
		this.taskRegistry = taskRegistry;
	}

	@Override
	public void execute(VariantAwareComponentInternal<?> component) {
		taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of("objects"), ObjectsLifecycleTask.class, component.getIdentifier())).configure(configureDependsOn(component.getDevelopmentVariant().flatMap(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS)));
	}
}
