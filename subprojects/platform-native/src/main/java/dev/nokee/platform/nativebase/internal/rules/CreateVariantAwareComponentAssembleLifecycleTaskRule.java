package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.internal.VariantAwareComponentInternal;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import org.gradle.api.Action;

import static dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TaskUtils.configureGroup;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP;

public class CreateVariantAwareComponentAssembleLifecycleTaskRule implements Action<VariantAwareComponentInternal<?>> {
	private final TaskRegistry taskRegistry;

	public CreateVariantAwareComponentAssembleLifecycleTaskRule(TaskRegistry taskRegistry) {
		this.taskRegistry = taskRegistry;
	}

	@Override
	public void execute(VariantAwareComponentInternal<?> component) {
		// The "component" assemble task was most likely added by the 'lifecycle-base' plugin
		//   then we configure the dependency.
		//   Note that the dependency may already exists for single variant component but it's not a big deal.
		taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), component.getIdentifier()), configureGroup(BUILD_GROUP))
			.configure(configureDependsOn(component.getDevelopmentVariant().flatMap(TO_DEVELOPMENT_BINARY)));
	}
}
