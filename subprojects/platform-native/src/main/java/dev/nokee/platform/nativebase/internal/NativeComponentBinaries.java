package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.NokeeMap;
import dev.nokee.model.internal.NokeeMapImpl;
import dev.nokee.model.internal.Value;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkBundleTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.val;
import org.gradle.api.model.ObjectFactory;

public class NativeComponentBinaries implements ComponentBinariesInternal<VariantIdentifier<?>> {
	private final TaskRegistry taskRegistry;
	private final BaseComponent<?> component;
	private final NokeeMap<BinaryIdentifier<? extends Binary>, Binary> store;
	private final ObjectFactory objectFactory;
	private final BinaryView<Binary> binaryView;

	public NativeComponentBinaries(TaskRegistry taskRegistry, BaseComponent<?> component, ObjectFactory objectFactory) {
		this.taskRegistry = taskRegistry;
		this.component = component;
		this.store = new NokeeMapImpl<>(Binary.class, objectFactory);
		this.objectFactory = objectFactory;
		this.binaryView = new BinaryViewImpl<>(store.entrySet());
	}

	@Override
	public void createBinaries(VariantIdentifier<?> identifier) {
		val buildVariant = (BuildVariantInternal) identifier.getBuildVariant();
		val targetMachineInternal = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));
		val names = component.getNames().forBuildVariant(buildVariant, component.getBuildVariants().get());
		val objectSourceSets = objectFactory.newInstance(NativeLanguageRules.class, names).apply(component.getSourceCollection());
		if (buildVariant.hasAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)) {
			DefaultBinaryLinkage linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
			if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
				val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkExecutableTask.class, identifier));
				val binary = objectFactory.newInstance(ExecutableBinaryInternal.class, names, objectSourceSets, targetMachineInternal, linkTask);
				store.put(BinaryIdentifier.of(BinaryName.of("executable"), ExecutableBinaryInternal.class, identifier), Value.fixed(binary));
			} else if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
				val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkSharedLibraryTask.class, identifier));

				val binary = objectFactory.newInstance(SharedLibraryBinaryInternal.class, names, objectFactory.domainObjectSet(LanguageSourceSetInternal.class), targetMachineInternal, objectSourceSets, linkTask);
				store.put(BinaryIdentifier.of(BinaryName.of("sharedLibrary"), SharedLibraryBinaryInternal.class, identifier), Value.fixed(binary));
			} else if (linkage.equals(DefaultBinaryLinkage.BUNDLE)) {
				val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkBundleTask.class, identifier));

				val binary = objectFactory.newInstance(BundleBinaryInternal.class, names, targetMachineInternal, objectSourceSets, linkTask);
				store.put(BinaryIdentifier.of(BinaryName.of("bundle"), BundleBinaryInternal.class, identifier), Value.fixed(binary));
			} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
				val createTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("create"), CreateStaticLibraryTask.class, identifier));

				val binary = objectFactory.newInstance(StaticLibraryBinaryInternal.class, names, objectSourceSets, targetMachineInternal, createTask);
				store.put(BinaryIdentifier.of(BinaryName.of("staticLibrary"), StaticLibraryBinaryInternal.class, identifier), Value.fixed(binary));
			}
		}
	}

	public void createBinaries(KnownVariant<?> knownVariant) {
		val identifier = knownVariant.getIdentifier();
		createBinaries(identifier);
	}

	@Override
	public BinaryView<Binary> getAsView() {
		return binaryView;
	}

	@Override
	public BinaryView<Binary> getAsViewFor(VariantIdentifier<?> identifier) {
		return new BinaryViewImpl<>(store.entrySet().filter(entry -> DomainObjectIdentifierUtils.isDescendent(entry.getKey(), identifier)));
	}

	@Override
	public <T extends Binary> void put(BinaryIdentifier<T> identifier, T binary) {
		store.put(identifier, Value.fixed(binary));
	}

	public NativeComponentBinaries disallowChanges() {
		store.disallowChanges();
		return this;
	}
}
