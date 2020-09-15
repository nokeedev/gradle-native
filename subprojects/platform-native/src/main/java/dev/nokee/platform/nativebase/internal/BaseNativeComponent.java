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
import dev.nokee.platform.nativebase.*;
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
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.emptyList;

public abstract class BaseNativeComponent<T extends VariantInternal> extends BaseComponent<T> {
	private final Class<T> variantType;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;
	@Getter(AccessLevel.PROTECTED) private final ProjectLayout layout;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;

	public BaseNativeComponent(NamingScheme names, Class<T> variantType, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations) {
		super(names, variantType, objects);
		this.providers = providers;
		this.tasks = tasks;
		this.layout = layout;
		this.configurations = configurations;
		Preconditions.checkArgument(BaseNativeVariant.class.isAssignableFrom(variantType));
		this.variantType = variantType;
		getDevelopmentVariant().convention(providers.provider(new BuildableDevelopmentVariantConvention<>(getVariantCollection()::get)));
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

	public void finalizeExtension(Project project) {
		// TODO: Assert build variant matches dimensions
		getBuildVariants().get().forEach(buildVariant -> {
			final DefaultTargetMachine targetMachineInternal = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));
			final NamingScheme names = this.getNames().forBuildVariant(buildVariant, getBuildVariants().get());

			val dependencies = newDependencies(names.withComponentDisplayName("main native component"), buildVariant);
			VariantProvider<T> variant = getVariantCollection().registerVariant(buildVariant, (name, bv) -> {
				T it = createVariant(name, bv, dependencies);

				DomainObjectSet<GeneratedSourceSet> objectSourceSets = getObjects().newInstance(NativeLanguageRules.class, names).apply(getSourceCollection());
				BaseNativeVariant variantInternal = (BaseNativeVariant)it;
				if (buildVariant.hasAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)) {
					DefaultBinaryLinkage linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
					if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
						TaskProvider<LinkExecutableTask> linkTask = getTasks().register(names.getTaskName("link"), LinkExecutableTask.class);
						ExecutableBinaryInternal binary = getObjects().newInstance(ExecutableBinaryInternal.class, names, objectSourceSets, targetMachineInternal, linkTask, dependencies.getIncoming());
						variantInternal.getBinaryCollection().add(binary);
						binary.getBaseName().convention(getBaseName());
					} else if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
						TaskProvider<LinkSharedLibraryTask> linkTask = getTasks().register(names.getTaskName("link"), LinkSharedLibraryTask.class);

						SharedLibraryBinaryInternal binary = getObjects().newInstance(SharedLibraryBinaryInternal.class, names, getObjects().domainObjectSet(LanguageSourceSetInternal.class), targetMachineInternal, objectSourceSets, linkTask, dependencies.getIncoming());
						variantInternal.getBinaryCollection().add(binary);
						binary.getBaseName().convention(getBaseName());
					} else if (linkage.equals(DefaultBinaryLinkage.BUNDLE)) {
						TaskProvider<LinkBundleTask> linkTask = getTasks().register(names.getTaskName("link"), LinkBundleTask.class);

						BundleBinaryInternal binary = getObjects().newInstance(BundleBinaryInternal.class, names, targetMachineInternal, objectSourceSets, linkTask, dependencies.getIncoming());
						variantInternal.getBinaryCollection().add(binary);
						binary.getBaseName().convention(getBaseName());
					} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
						TaskProvider<CreateStaticLibraryTask> createTask = getTasks().register(names.getTaskName("create"), CreateStaticLibraryTask.class);

						val binary = getObjects().newInstance(StaticLibraryBinaryInternal.class, names, objectSourceSets, targetMachineInternal, createTask, dependencies.getIncoming());
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

				return it;
			});

			onEachVariantDependencies(variant, dependencies);

			getTasks().register(names.getTaskName("objects"), task -> {
				task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
				task.setDescription("Assembles main objects.");
				task.dependsOn(variant.map(it -> it.getBinaries().withType(ExecutableBinary.class).map(ExecutableBinary::getCompileTasks)));
				task.dependsOn(variant.map(it -> it.getBinaries().withType(SharedLibraryBinary.class).map(SharedLibraryBinary::getCompileTasks)));
				task.dependsOn(variant.map(it -> it.getBinaries().withType(StaticLibraryBinary.class).map(StaticLibraryBinary::getCompileTasks)));
			});

			onEachVariant(buildVariant, variant, names);

			if (getBuildVariants().get().size() > 1) {
				getTasks().register(names.getTaskName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME), task -> {
					task.dependsOn(variant.flatMap(Variant::getDevelopmentBinary));
					task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
				});
			}
		});

		getTasks().named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME, task -> {
			task.dependsOn(getDevelopmentVariant().flatMap(Variant::getDevelopmentBinary));
		});

		if (!getTasks().getNames().contains("objects") && getNames().getComponentName().equals("main")) {
			getTasks().register("objects", task -> {
				task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
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
	protected void onEachVariant(BuildVariantInternal buildVariant, VariantProvider<T> variant, NamingScheme names) {
		// TODO: This is dependent per component, for example, iOS will have different target.
		//  It should be moved lower to the "general" native component
		if (buildVariant.hasAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)) {
			DefaultBinaryLinkage linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
			if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
				getTasks().register(names.getTaskName("sharedLibrary"), task -> {
					task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
					task.setDescription("Assembles a shared library binary containing the main objects.");
					task.dependsOn(variant.map(it -> ((SharedLibraryBinary)it.getDevelopmentBinary().get()).getLinkTask()));
				});
			} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
				getTasks().register(names.getTaskName("staticLibrary"), task -> {
					task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
					task.setDescription("Assembles a static library binary containing the main objects.");
					task.dependsOn(variant.map(it -> ((StaticLibraryBinary)it.getDevelopmentBinary().get()).getCreateTask()));
				});
			} else if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
				getTasks().register(names.getTaskName("executable"), task -> {
					task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
					task.setDescription("Assembles a executable binary containing the main objects.");
					task.dependsOn(variant.map(it -> ((ExecutableBinary)it.getDevelopmentBinary().get()).getLinkTask()));
				});
			}
		}
	}
}
