package dev.nokee.platform.nativebase.internal;

import com.google.common.base.Preconditions;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.cpp.internal.CppHeaderSet;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import dev.nokee.platform.nativebase.*;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
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
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.ASSEMBLE_TASK_NAME;
import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP;

public abstract class BaseNativeComponent<T extends VariantInternal> extends BaseComponent<T> {
	private final Class<T> variantType;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter(AccessLevel.PROTECTED) private final ProjectLayout layout;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	private final TaskRegistry taskRegistry;

	public BaseNativeComponent(NamingScheme names, Class<T> variantType, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations) {
		super(names, variantType, objects);
		this.providers = providers;
		this.layout = layout;
		this.configurations = configurations;
		Preconditions.checkArgument(BaseNativeVariant.class.isAssignableFrom(variantType));
		this.variantType = variantType;
		getDevelopmentVariant().convention(providers.provider(new BuildableDevelopmentVariantConvention<>(getVariantCollection()::get)));
		this.taskRegistry = new TaskRegistryImpl(tasks);
	}

	public abstract NativeComponentDependencies getDependencies();

	public VariantView<T> getVariants() {
		return getVariantCollection().getAsView(variantType);
	}

	public static <T> T one(Iterable<T> c) {
		Iterator<T> iterator = c.iterator();
		Preconditions.checkArgument(iterator.hasNext(), "collection needs to have one element, was empty");
		T result = iterator.next();
		Preconditions.checkArgument(!iterator.hasNext(), "collection needs to only have one element, more than one element found");
		return result;
	}

	protected abstract T createVariant(String name, BuildVariantInternal buildVariant, VariantComponentDependencies<?> dependencies);

	protected abstract VariantComponentDependencies<?> newDependencies(NamingScheme names, BuildVariantInternal buildVariant);

	protected Action<T> createBinariesFor(VariantIdentifier<T> variantIdentifier) {
		val buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
		final DefaultTargetMachine targetMachineInternal = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));

		return it -> {
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
		};
	}

	public void finalizeExtension(Project project) {
		getVariantCollection().whenElementKnown(knownVariant -> {
			knownVariant.configure(createBinariesFor(knownVariant.getIdentifier()));

			taskRegistry.register(TaskIdentifier.of(TaskName.of("objects"), knownVariant.getIdentifier()), task -> {
				task.setGroup(BUILD_GROUP);
				task.setDescription("Assembles main objects.");
				task.dependsOn(knownVariant.map(it -> it.getBinaries().withType(ExecutableBinary.class).map(ExecutableBinary::getCompileTasks)));
				task.dependsOn(knownVariant.map(it -> it.getBinaries().withType(SharedLibraryBinary.class).map(SharedLibraryBinary::getCompileTasks)));
				task.dependsOn(knownVariant.map(it -> it.getBinaries().withType(StaticLibraryBinary.class).map(StaticLibraryBinary::getCompileTasks)));
			});

			onEachVariant(knownVariant);

			if (getBuildVariants().get().size() > 1) {
				taskRegistry.register(TaskIdentifier.of(TaskName.of(ASSEMBLE_TASK_NAME), knownVariant.getIdentifier()), task -> {
					task.dependsOn(knownVariant.flatMap(Variant::getDevelopmentBinary));
					task.setGroup(BUILD_GROUP);
				});
			}
		});

		getBuildVariants().get().forEach(buildVariant -> {
			final NamingScheme names = this.getNames().forBuildVariant(buildVariant, getBuildVariants().get());
			final VariantIdentifier<T> variantIdentifier = VariantIdentifier.builder().withUnambiguousNameFromBuildVariants(buildVariant, getBuildVariants().get()).withComponentIdentifier(getIdentifier()).withType(variantType).build();

			val dependencies = newDependencies(names.withComponentDisplayName("main native component"), buildVariant);
			VariantProvider<T> variant = getVariantCollection().registerVariant(variantIdentifier, (name, bv) -> createVariant(name, bv, dependencies));

			onEachVariantDependencies(variant, dependencies);
		});

		// Make sure the task exists and configure it!
		taskRegistry.registerIfAbsent(ASSEMBLE_TASK_NAME).configure(task -> {
			task.dependsOn(getDevelopmentVariant().flatMap(Variant::getDevelopmentBinary));
		});

		if (getIdentifier().isMainComponent()) {
			taskRegistry.registerIfAbsent("objects", task -> {
				task.setGroup(BUILD_GROUP);
				task.setDescription("Assembles main objects.");
				task.dependsOn(getDevelopmentVariant().flatMap(it -> {
					if (it.getDevelopmentBinary().get() instanceof NativeBinary) {
						return ((NativeBinary) it.getDevelopmentBinary().get()).getCompileTasks().getElements();
					}
					return getProviders().provider(() -> emptyList());
				}));
			});
		}

		// TODO: This may need to be moved somewhere else.
		// finalize the variantCollection
		getVariantCollection().disallowChanges();
	}

	protected void onEachVariantDependencies(VariantProvider<T> variant, VariantComponentDependencies<?> dependencies) {
		if (NativeLibrary.class.isAssignableFrom(variantType)) {
			if (!getSourceCollection().withType(SwiftSourceSet.class).isEmpty()) {
				dependencies.getOutgoing().getExportedSwiftModule().convention(variant.flatMap(it -> {
					List<? extends Provider<RegularFile>> result = it.getBinaries().withType(NativeBinary.class).flatMap(binary -> {
						List<? extends Provider<RegularFile>> modules = binary.getCompileTasks().withType(SwiftCompileTask.class).map(task -> task.getModuleFile()).get();
						return modules;
					}).get();
					return one(result);
				}));
			}
			getSourceCollection().matching(it -> (it instanceof CHeaderSet || it instanceof CppHeaderSet) && it.getName().equals("public")).configureEach(sourceSet -> {
				// TODO: Allow to export more than one folder
				File directory = null;
				if (sourceSet instanceof CHeaderSet) {
					directory = ((CHeaderSet) sourceSet).getHeaderDirectory();
				} else if (sourceSet instanceof CppHeaderSet) {
					directory = ((CppHeaderSet) sourceSet).getHeaderDirectory();
				}

				dependencies.getOutgoing().getExportedHeaders().fileValue(directory);
			});
		}
		dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
	}

	// TODO: BuildVariant and NamedDomainObjectProvider from VariantCollection should be together.
	protected void onEachVariant(KnownVariant<T> variant) {
		val variantIdentifier = variant.getIdentifier();
		val buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
		// TODO: This is dependent per component, for example, iOS will have different target.
		//  It should be moved lower to the "general" native component
		if (buildVariant.hasAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)) {
			DefaultBinaryLinkage linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
			if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
				taskRegistry.register(TaskIdentifier.of(TaskName.of("sharedLibrary"), variantIdentifier), task -> {
					task.setGroup(BUILD_GROUP);
					task.setDescription("Assembles a shared library binary containing the main objects.");
					task.dependsOn(variant.map(it -> ((SharedLibraryBinary)it.getDevelopmentBinary().get()).getLinkTask()));
				});
			} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
				taskRegistry.register(TaskIdentifier.of(TaskName.of("staticLibrary"), variantIdentifier), task -> {
					task.setGroup(BUILD_GROUP);
					task.setDescription("Assembles a static library binary containing the main objects.");
					task.dependsOn(variant.map(it -> ((StaticLibraryBinary)it.getDevelopmentBinary().get()).getCreateTask()));
				});
			} else if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
				taskRegistry.register(TaskIdentifier.of(TaskName.of("executable"), variantIdentifier), task -> {
					task.setGroup(BUILD_GROUP);
					task.setDescription("Assembles a executable binary containing the main objects.");
					task.dependsOn(variant.map(it -> ((ExecutableBinary)it.getDevelopmentBinary().get()).getLinkTask()));
				});
			}
		}
	}
}
