package dev.nokee.platform.nativebase.internal;

import com.google.common.base.Preconditions;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.NamingScheme;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import java.util.Iterator;

public class BaseNativeVariant extends BaseVariant {
	@Getter private final NamingScheme names;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;

	public BaseNativeVariant(String name, NamingScheme names, BuildVariantInternal buildVariant, ObjectFactory objects, TaskContainer tasks, ProviderFactory providers) {
		super(name, buildVariant, objects);
		this.names = names;
		this.tasks = tasks;
		this.providers = providers;

		getDevelopmentBinary().convention(getDefaultBinary());
	}

	public TaskProvider<Task> getAssembleTask() {
		return getTasks().named(names.getTaskName("assemble"));
	}

	protected Provider<Binary> getDefaultBinary() {
		return getProviders().provider(() -> {
			DefaultBinaryLinkage linkage = getBuildVariant().getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
			if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
				return one(getBinaryCollection().withType(ExecutableBinaryInternal.class));
			} else if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
				return one(getBinaryCollection().withType(SharedLibraryBinaryInternal.class));
			} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
				return one(getBinaryCollection().withType(StaticLibraryBinaryInternal.class));
			}
			return null;
		});
	}

	protected static <T> T one(Iterable<T> c) {
		Iterator<T> iterator = c.iterator();
		Preconditions.checkArgument(iterator.hasNext(), "collection needs to have one element, was empty");
		T result = iterator.next();
		Preconditions.checkArgument(!iterator.hasNext(), "collection needs to only have one element, more than one element found");
		return result;
	}
}
