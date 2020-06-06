package dev.nokee.platform.nativebase.internal;

import com.google.common.base.Preconditions;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import lombok.Getter;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.Iterator;

public abstract class BaseNativeVariant extends BaseVariant {
	@Getter private final NamingScheme names;

	public BaseNativeVariant(String name, NamingScheme names, BuildVariant buildVariant) {
		super(name, buildVariant);
		this.names = names;

		getDevelopmentBinary().convention(getDefaultBinary());
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProviderFactory getProviders();

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
