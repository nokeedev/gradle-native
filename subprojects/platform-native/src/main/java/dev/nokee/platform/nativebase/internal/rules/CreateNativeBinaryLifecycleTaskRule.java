package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.KnownVariant;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.platform.nativebase.internal.tasks.ExecutableLifecycleTask;
import dev.nokee.platform.nativebase.internal.tasks.SharedLibraryLifecycleTask;
import dev.nokee.platform.nativebase.internal.tasks.StaticLibraryLifecycleTask;
import lombok.Value;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;

import java.util.HashMap;
import java.util.Map;

import static dev.nokee.platform.nativebase.internal.rules.ToDevelopmentBinaryTransformer.TO_DEVELOPMENT_BINARY;
import static dev.nokee.utils.TaskUtils.configureDependsOn;

public class CreateNativeBinaryLifecycleTaskRule implements Action<KnownVariant<? extends Variant>> {
	private static final Map<DefaultBinaryLinkage, LifecycleTaskConfiguration> TASK_CONFIGURATIONS = new HashMap<>();

	static {
		TASK_CONFIGURATIONS.put(DefaultBinaryLinkage.SHARED, LifecycleTaskConfiguration.of(TaskName.of("sharedLibrary"), SharedLibraryLifecycleTask.class));
		TASK_CONFIGURATIONS.put(DefaultBinaryLinkage.STATIC, LifecycleTaskConfiguration.of(TaskName.of("staticLibrary"), StaticLibraryLifecycleTask.class));
		TASK_CONFIGURATIONS.put(DefaultBinaryLinkage.EXECUTABLE, LifecycleTaskConfiguration.of(TaskName.of("executable"), ExecutableLifecycleTask.class));
	}

	private final TaskRegistry taskRegistry;

	public CreateNativeBinaryLifecycleTaskRule(TaskRegistry taskRegistry) {
		this.taskRegistry = taskRegistry;
	}

	@Override
	public void execute(KnownVariant<? extends Variant> knownVariant) {
		val variantIdentifier = knownVariant.getIdentifier();
		val buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
		if (buildVariant.hasAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)) {
			val linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
			val taskConfiguration = TASK_CONFIGURATIONS.computeIfAbsent(linkage, CreateNativeBinaryLifecycleTaskRule::throwUnknownLinkageException);
			taskRegistry.register(TaskIdentifier.of(taskConfiguration.name, taskConfiguration.taskType, variantIdentifier), configureDependsOn(knownVariant.map(TO_DEVELOPMENT_BINARY)));
		}
	}

	private static LifecycleTaskConfiguration throwUnknownLinkageException(DefaultBinaryLinkage linkage) {
		throw new IllegalArgumentException(String.format("Unknown linkage '%s'.", linkage.getName()));
	}

	@Value(staticConstructor = "of")
	private static class LifecycleTaskConfiguration {
		TaskName name;
		Class<? extends Task> taskType;
	}
}
