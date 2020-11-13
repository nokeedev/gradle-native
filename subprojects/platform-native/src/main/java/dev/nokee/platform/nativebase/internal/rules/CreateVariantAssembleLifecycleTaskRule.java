package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.variants.KnownVariant;
import lombok.val;
import org.gradle.api.Action;

import static dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY;
import static dev.nokee.utils.TaskUtils.configureDependsOn;
import static dev.nokee.utils.TaskUtils.configureGroup;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP;

public class CreateVariantAssembleLifecycleTaskRule implements Action<KnownVariant<? extends Variant>> {
	private final TaskRegistry taskRegistry;

	public CreateVariantAssembleLifecycleTaskRule(TaskRegistry taskRegistry) {
		this.taskRegistry = taskRegistry;
	}

	@Override
	public void execute(KnownVariant<? extends Variant> knownVariant) {
		// For single variant component, the task may already exists, coming from 'lifecycle-base'.
		val assembleTask = taskRegistry.registerIfAbsent(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), knownVariant.getIdentifier()), configureGroup(BUILD_GROUP));

		// Only multi-variant component should attach the proper dependency to the assemble task.
		//   Single variant depends on the a more complex logic around buildability, see CreateVariantAwareComponentAssembleLifecycleTaskRule
		if (!knownVariant.getIdentifier().getAmbiguousDimensions().get().isEmpty()) {
			assembleTask.configure(configureDependsOn(knownVariant.flatMap(TO_DEVELOPMENT_BINARY)));
		}
	}
}
