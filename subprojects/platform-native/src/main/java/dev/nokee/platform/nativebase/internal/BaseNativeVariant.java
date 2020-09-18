package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.nativebase.internal.rules.NativeDevelopmentBinaryConvention;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

public class BaseNativeVariant extends BaseVariant {
	@Getter private final NamingScheme names;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;

	public BaseNativeVariant(VariantIdentifier<?> identifier, NamingScheme names, ObjectFactory objects, TaskContainer tasks, ProviderFactory providers) {
		super(identifier, objects);
		this.names = names;
		this.tasks = tasks;
		this.providers = providers;

		getDevelopmentBinary().convention(getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(getBuildVariant().getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE))));
	}

	public TaskProvider<Task> getAssembleTask() {
		return getTasks().named(names.getTaskName("assemble"));
	}
}
