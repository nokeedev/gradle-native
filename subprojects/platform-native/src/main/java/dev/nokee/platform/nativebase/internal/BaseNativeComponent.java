package dev.nokee.platform.nativebase.internal;

import com.google.common.base.Preconditions;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.language.c.internal.CSourceSet;
import dev.nokee.language.c.internal.CSourceSetTransform;
import dev.nokee.language.cpp.internal.CppSourceSet;
import dev.nokee.language.cpp.internal.CppSourceSetTransform;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetTransform;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetTransform;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.language.swift.internal.SwiftSourceSetTransform;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.runtime.nativebase.internal.DefaultOperatingSystemFamily;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.Cast;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseNativeComponent<T extends Variant> extends BaseComponent<T> {
	private final DomainObjectSet<SourceSet> sourceCollection = getObjects().domainObjectSet(SourceSet.class);
	private final Class<T> variantType;

	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	public BaseNativeComponent(NamingScheme names, Class<T> variantType) {
		super(names, variantType);
		Preconditions.checkArgument(BaseNativeVariant.class.isAssignableFrom(variantType));
		this.variantType = variantType;
	}

	public abstract AbstractNativeComponentDependencies getDependencies();

	public DomainObjectSet<SourceSet> getSourceCollection() {
		return sourceCollection;
	}

	public VariantView<T> getVariants() {
		return getVariantCollection().getAsView(variantType);
	}

	public Provider<? extends BaseNativeVariant> getDevelopmentVariant() {
		return getProviders().provider(() -> {
			List<BaseNativeVariant> variants = getVariantCollection().get().stream().map(it -> {
				Preconditions.checkArgument(it instanceof BaseNativeVariant);
				return (BaseNativeVariant) it;
			}).filter(it -> {
				DefaultOperatingSystemFamily osFamily = it.getBuildVariant().getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE);
				DefaultMachineArchitecture architecture = it.getBuildVariant().getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE);
				if (DefaultOperatingSystemFamily.HOST.equals(osFamily) && DefaultMachineArchitecture.HOST.equals(architecture)) {
					return true;
				}
				return false;
			}).collect(Collectors.toList());

			if (variants.isEmpty()) {
				return null;
			}
			return one(variants);
		});
	}

	protected static <T> T one(Iterable<T> c) {
		Iterator<T> iterator = c.iterator();
		Preconditions.checkArgument(iterator.hasNext(), "collection needs to have one element, was empty");
		T result = iterator.next();
		Preconditions.checkArgument(!iterator.hasNext(), "collection needs to only have one element, more than one element found");
		return result;
	}

	public void finalizeExtension(Project project) {
		// TODO: Assert build variant matches dimensions
		getBuildVariants().get().forEach(buildVariant -> {
			final DefaultTargetMachine targetMachineInternal = new DefaultTargetMachine(buildVariant.getAxisValue(DefaultOperatingSystemFamily.DIMENSION_TYPE), buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE));
			final NamingScheme names = this.getNames().forBuildVariant(buildVariant, getBuildVariants().get());

			NamedDomainObjectProvider<? extends BaseNativeVariant> variant = Cast.uncheckedCast(getVariantCollection().registerVariant(buildVariant, it -> {
				ConfigurationUtils configurationUtils = getObjects().newInstance(ConfigurationUtils.class);
				// TODO: This is not correct for Swift
				Configuration headerSearchPaths = getConfigurations().create(names.getConfigurationName("headerSearchPaths"), configurationUtils.asIncomingHeaderSearchPathFrom(getDependencies().getImplementationDependencies(), getDependencies().getCompileOnlyDependencies()));

				DomainObjectSet<GeneratedSourceSet> objectSourceSets = getObjects().domainObjectSet(GeneratedSourceSet.class);
				getSourceCollection().withType(CSourceSet.class).configureEach(s -> objectSourceSets.add(getObjects().newInstance(CSourceSetTransform.class, names, headerSearchPaths).transform(s)));
				getSourceCollection().withType(CppSourceSet.class).configureEach(s -> objectSourceSets.add(getObjects().newInstance(CppSourceSetTransform.class, names, headerSearchPaths).transform(s)));
				getSourceCollection().withType(ObjectiveCSourceSet.class).configureEach(s -> objectSourceSets.add(getObjects().newInstance(ObjectiveCSourceSetTransform.class, names, headerSearchPaths).transform(s)));
				getSourceCollection().withType(ObjectiveCppSourceSet.class).configureEach(s -> objectSourceSets.add(getObjects().newInstance(ObjectiveCppSourceSetTransform.class, names, headerSearchPaths).transform(s)));
				getSourceCollection().withType(SwiftSourceSet.class).configureEach(s -> objectSourceSets.add(getObjects().newInstance(SwiftSourceSetTransform.class, names, headerSearchPaths).transform(s)));

				BaseNativeVariant variantInternal = (BaseNativeVariant)it;
				if (buildVariant.hasAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)) {
					DefaultBinaryLinkage linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
					if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
						TaskProvider<LinkExecutableTask> linkTask = getTasks().register(names.getTaskName("link"), LinkExecutableTask.class);
						ExecutableBinaryInternal binary = getObjects().newInstance(ExecutableBinaryInternal.class, names, objectSourceSets, targetMachineInternal, linkTask);
						variantInternal.getBinaryCollection().add(binary);
						binary.getBaseName().convention(project.getName());
					} else if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
						TaskProvider<LinkSharedLibraryTask> linkTask = getTasks().register(names.getTaskName("link"), LinkSharedLibraryTask.class);

						// TODO: Dependencies should be coming from variant
						SharedLibraryBinaryInternal binary = getObjects().newInstance(SharedLibraryBinaryInternal.class, names, getObjects().domainObjectSet(LanguageSourceSetInternal.class), getDependencies().getImplementationDependencies(), targetMachineInternal, objectSourceSets, linkTask, getDependencies().getLinkOnlyDependencies());
						variantInternal.getBinaryCollection().add(binary);
						binary.getBaseName().convention(project.getName());

//					} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
//						variantInternal.getBinaryCollection().add(getObjects().newInstance(ExecutableBinaryInternal.class, names, objectSourceSets));
					}
				}
			}));

			getTasks().register(names.getTaskName("objects"), task -> {
				task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
				task.setDescription("Assembles main objects.");
				task.dependsOn(variant.map(it -> it.getBinaries().withType(ExecutableBinary.class).map(ExecutableBinary::getCompileTasks)));
				task.dependsOn(variant.map(it -> it.getBinaries().withType(SharedLibraryBinary.class).map(SharedLibraryBinary::getCompileTasks)));
				task.dependsOn(variant.map(it -> it.getBinaries().withType(StaticLibraryBinary.class).map(StaticLibraryBinary::getCompileTasks)));
			});

			onEachVariant(buildVariant, variant);

			if (getBuildVariants().get().size() > 1) {
				getTasks().register(names.getTaskName(LifecycleBasePlugin.ASSEMBLE_TASK_NAME), task -> {
					task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
				});
			}
		});

		getTasks().named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME, task -> {
			task.dependsOn(getDevelopmentVariant().flatMap(BaseNativeVariant::getDevelopmentBinary));
		});

		// TODO: This may need to be moved somewhere else.
		// finalize the variantCollection
		getVariantCollection().disallowChanges();
	}

	// TODO: BuildVariant and NamedDomainObjectProvider from VariantCollection should be together.
	protected void onEachVariant(BuildVariant buildVariant, NamedDomainObjectProvider<? extends BaseNativeVariant> variant) {
		// TODO: This is dependent per component, for example, iOS will have different target.
		//  It should be moved lower to the "general" native component
		if (buildVariant.hasAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)) {
			DefaultBinaryLinkage linkage = buildVariant.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE);
			if (linkage.equals(DefaultBinaryLinkage.SHARED)) {
				getTasks().register(getNames().getTaskName("sharedLibrary"), task -> {
					task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
					task.setDescription("Assembles a shared library binary containing the main objects.");
					task.dependsOn(variant.map(it -> ((SharedLibraryBinary)it.getDevelopmentBinary().get()).getLinkTask()));
				});
			} else if (linkage.equals(DefaultBinaryLinkage.STATIC)) {
				getTasks().register(getNames().getTaskName("staticLibrary"), task -> {
					task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
					task.setDescription("Assembles a static library binary containing the main objects.");
					task.dependsOn(variant.map(it -> ((StaticLibraryBinary)it.getDevelopmentBinary().get()).getCreateTask()));
				});
			} else if (linkage.equals(DefaultBinaryLinkage.EXECUTABLE)) {
				getTasks().register(getNames().getTaskName("executable"), task -> {
					task.setGroup(LifecycleBasePlugin.BUILD_GROUP);
					task.setDescription("Assembles a executable binary containing the main objects.");
					task.dependsOn(variant.map(it -> ((ExecutableBinary)it.getDevelopmentBinary().get()).getLinkTask()));
				});
			}
		}
	}
}
