package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.variants.KnownVariant;
import dev.nokee.platform.nativebase.internal.tasks.ObjectsLifecycleTask;
import org.gradle.api.Action;

import static dev.nokee.utils.TaskUtils.configureDependsOn;

public class CreateVariantObjectsLifecycleTaskRule implements Action<KnownVariant<? extends Variant>> {
	private final TaskRegistry taskRegistry;

	public CreateVariantObjectsLifecycleTaskRule(TaskRegistry taskRegistry) {
		this.taskRegistry = taskRegistry;
	}

	@Override
	public void execute(KnownVariant<? extends Variant> knownVariant) {
		taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of("objects"), ObjectsLifecycleTask.class, knownVariant.getIdentifier())).configure(configureDependsOn(knownVariant.flatMap(ToBinariesCompileTasksTransformer.TO_DEVELOPMENT_BINARY_COMPILE_TASKS)));
	}
}
