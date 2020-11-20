package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;

public class BaseNativeVariant extends BaseVariant {
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	private final TaskProvider<Task> assembleTask;

	public BaseNativeVariant(VariantIdentifier<?> identifier, ObjectFactory objects, ProviderFactory providers, TaskProvider<Task> assembleTask, BinaryViewFactory binaryViewFactory) {
		super(identifier, objects, binaryViewFactory);
		this.providers = providers;
		this.assembleTask = assembleTask;
	}

	public TaskProvider<Task> getAssembleTask() {
		return assembleTask;
	}
}
