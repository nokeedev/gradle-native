package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.cpp.internal.CppHeaderSet;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.KnownVariant;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkBundleTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.Getter;
import lombok.val;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

public class NativeComponentBinaries {
	@Getter private final DomainObjectSet<Binary> binaryCollection;
	private final ObjectFactory objectFactory;
	private final BaseNativeComponent<?> component;
	private final TaskRegistry taskRegistry;

	public NativeComponentBinaries(ObjectFactory objectFactory, BaseNativeComponent<?> component, TaskRegistry taskRegistry) {
		this.binaryCollection = objectFactory.domainObjectSet(Binary.class);
		this.objectFactory = objectFactory;
		this.component = component;
		this.taskRegistry = taskRegistry;
	}

	public void createBinaries(KnownVariant<? extends VariantInternal> knownVariant) {
		val variantIdentifier = knownVariant.getIdentifier();
		val buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
		final DefaultTargetMachine targetMachineInternal = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));

		knownVariant.configure(it -> {
			val incomingDependencies = (NativeIncomingDependencies) it.getResolvableDependencies();
			val names = component.getNames().forBuildVariant(buildVariant, component.getBuildVariants().get());
			DomainObjectSet<GeneratedSourceSet> objectSourceSets = objectFactory.newInstance(NativeLanguageRules.class, names).apply(component.getSourceCollection());
			BaseNativeVariant variantInternal = (BaseNativeVariant)it;
			if (buildVariant.hasAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)) {
				DefaultBinaryLinkage linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
				if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
					val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkExecutableTask.class, variantIdentifier));
					ExecutableBinaryInternal binary = objectFactory.newInstance(ExecutableBinaryInternal.class, names, objectSourceSets, targetMachineInternal, linkTask, incomingDependencies);
					variantInternal.getBinaryCollection().add(binary);
					binary.getBaseName().convention(component.getBaseName());
				} else if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
					val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkSharedLibraryTask.class, variantIdentifier));

					SharedLibraryBinaryInternal binary = objectFactory.newInstance(SharedLibraryBinaryInternal.class, names, objectFactory.domainObjectSet(LanguageSourceSetInternal.class), targetMachineInternal, objectSourceSets, linkTask, incomingDependencies);
					variantInternal.getBinaryCollection().add(binary);
					binary.getBaseName().convention(component.getBaseName());
				} else if (linkage.equals(DefaultBinaryLinkage.BUNDLE)) {
					val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkBundleTask.class, variantIdentifier));

					BundleBinaryInternal binary = objectFactory.newInstance(BundleBinaryInternal.class, names, targetMachineInternal, objectSourceSets, linkTask, incomingDependencies);
					variantInternal.getBinaryCollection().add(binary);
					binary.getBaseName().convention(component.getBaseName());
				} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
					val createTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("create"), CreateStaticLibraryTask.class, variantIdentifier));

					val binary = objectFactory.newInstance(StaticLibraryBinaryInternal.class, names, objectSourceSets, targetMachineInternal, createTask, incomingDependencies);
					variantInternal.getBinaryCollection().add(binary);
					binary.getBaseName().convention(component.getBaseName());
				}
			}
			it.getBinaries().configureEach(NativeBinary.class, binary -> {
				binary.getCompileTasks().configureEach(NativeSourceCompile.class, task -> {
					val taskInternal = (AbstractNativeCompileTask) task;
					component.getSourceCollection().withType(CHeaderSet.class).configureEach(sourceSet -> {
						taskInternal.getIncludes().from(sourceSet.getSourceDirectorySet().getSourceDirectories());
					});
					component.getSourceCollection().withType(CppHeaderSet.class).configureEach(sourceSet -> {
						taskInternal.getIncludes().from(sourceSet.getSourceDirectorySet().getSourceDirectories());
					});
				});
			});
		});
	}
}
