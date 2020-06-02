package dev.nokee.platform.nativebase.internal;

import com.google.common.base.Preconditions;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.nativebase.NativeBinary;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.util.Iterator;

public abstract class BaseNativeVariant implements Named {
	private final String name;
	private final NamingScheme names;
	private final BuildVariant buildVariant;
	private final DomainObjectSet<BinaryInternal> binaryCollection;

	public BaseNativeVariant(String name, NamingScheme names, BuildVariant buildVariant) {
		this.name = name;
		this.names = names;
		this.buildVariant = buildVariant;
		this.binaryCollection = getObjects().domainObjectSet(BinaryInternal.class);
	}

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Override
	public String getName() {
		return name;
	}

	public BuildVariant getBuildVariant() {
		return buildVariant;
	}

	public BinaryView<Binary> getBinaries() {
		return Cast.uncheckedCast(getObjects().newInstance(DefaultBinaryView.class, binaryCollection, (Realizable)() -> {}));
	}

	public TaskProvider<Task> getAssembleTask() {
		return getTasks().named(names.getTaskName("assemble"));
	}

	public Provider<? extends NativeBinary> getDevelopmentBinary() {
		return getProviders().provider(() -> {
			DefaultBinaryLinkage linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
			if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
				return one(binaryCollection.withType(ExecutableBinaryInternal.class));
			} else if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
				return one(binaryCollection.withType(SharedLibraryBinaryInternal.class));
			} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
				return one(binaryCollection.withType(StaticLibraryBinaryInternal.class));
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

	public DomainObjectSet<BinaryInternal> getBinaryCollection() {
		return binaryCollection;
	}
}
