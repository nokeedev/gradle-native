package dev.nokee.platform.nativebase.internal;

import com.google.common.base.Preconditions;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.cpp.internal.CppHeaderSet;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkBundleTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

public abstract class BaseNativeComponent<T extends VariantInternal> extends BaseComponent<T> implements VariantAwareComponentInternal<T> {
	private final Class<T> variantType;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter(AccessLevel.PROTECTED) private final ProjectLayout layout;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	private final TaskRegistry taskRegistry;

	public BaseNativeComponent(ComponentIdentifier<?> identifier, NamingScheme names, Class<T> variantType, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations) {
		super(identifier, names, variantType, objects);
		this.providers = providers;
		this.layout = layout;
		this.configurations = configurations;
		Preconditions.checkArgument(BaseNativeVariant.class.isAssignableFrom(variantType));
		this.variantType = variantType;
		getDevelopmentVariant().convention(providers.provider(new BuildableDevelopmentVariantConvention<>(() -> getVariantCollection().get())));
		this.taskRegistry = new TaskRegistryImpl(tasks);
	}

	public abstract NativeComponentDependencies getDependencies();

	public VariantView<T> getVariants() {
		return getVariantCollection().getAsView(variantType);
	}

	protected void createBinaries(KnownVariant<T> knownVariant) {
		val variantIdentifier = knownVariant.getIdentifier();
		val buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
		final DefaultTargetMachine targetMachineInternal = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));

		knownVariant.configure(it -> {
			val incomingDependencies = (NativeIncomingDependencies) it.getResolvableDependencies();
			val names = this.getNames().forBuildVariant(buildVariant, getBuildVariants().get());
			DomainObjectSet<GeneratedSourceSet> objectSourceSets = getObjects().newInstance(NativeLanguageRules.class, names).apply(getSourceCollection());
			BaseNativeVariant variantInternal = (BaseNativeVariant)it;
			if (buildVariant.hasAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)) {
				DefaultBinaryLinkage linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
				if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
					val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkExecutableTask.class, variantIdentifier));
					ExecutableBinaryInternal binary = getObjects().newInstance(ExecutableBinaryInternal.class, names, objectSourceSets, targetMachineInternal, linkTask, incomingDependencies);
					variantInternal.getBinaryCollection().add(binary);
					binary.getBaseName().convention(getBaseName());
				} else if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
					val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkSharedLibraryTask.class, variantIdentifier));

					SharedLibraryBinaryInternal binary = getObjects().newInstance(SharedLibraryBinaryInternal.class, names, getObjects().domainObjectSet(LanguageSourceSetInternal.class), targetMachineInternal, objectSourceSets, linkTask, incomingDependencies);
					variantInternal.getBinaryCollection().add(binary);
					binary.getBaseName().convention(getBaseName());
				} else if (linkage.equals(DefaultBinaryLinkage.BUNDLE)) {
					val linkTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("link"), LinkBundleTask.class, variantIdentifier));

					BundleBinaryInternal binary = getObjects().newInstance(BundleBinaryInternal.class, names, targetMachineInternal, objectSourceSets, linkTask, incomingDependencies);
					variantInternal.getBinaryCollection().add(binary);
					binary.getBaseName().convention(getBaseName());
				} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
					val createTask = taskRegistry.register(TaskIdentifier.of(TaskName.of("create"), CreateStaticLibraryTask.class, variantIdentifier));

					val binary = getObjects().newInstance(StaticLibraryBinaryInternal.class, names, objectSourceSets, targetMachineInternal, createTask, incomingDependencies);
					variantInternal.getBinaryCollection().add(binary);
					binary.getBaseName().convention(getBaseName());
				}
			}
			it.getBinaries().configureEach(NativeBinary.class, binary -> {
				binary.getCompileTasks().configureEach(NativeSourceCompile.class, task -> {
					val taskInternal = (AbstractNativeCompileTask) task;
					getSourceCollection().withType(CHeaderSet.class).configureEach(sourceSet -> {
						taskInternal.getIncludes().from(sourceSet.getSourceDirectorySet().getSourceDirectories());
					});
					getSourceCollection().withType(CppHeaderSet.class).configureEach(sourceSet -> {
						taskInternal.getIncludes().from(sourceSet.getSourceDirectorySet().getSourceDirectories());
					});
				});
			});
		});
	}
}
