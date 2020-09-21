package dev.nokee.platform.nativebase.internal;

import com.google.common.base.Preconditions;
import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.cpp.internal.CppHeaderSet;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.tasks.TaskRegistry;
import dev.nokee.platform.base.internal.tasks.TaskRegistryImpl;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
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
import java.util.stream.Collectors;

public abstract class BaseNativeComponent<T extends VariantInternal> extends BaseComponent<T> implements VariantAwareComponentInternal<T> {
	private final Class<T> variantType;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	@Getter(AccessLevel.PROTECTED) private final ProjectLayout layout;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	private final TaskRegistry taskRegistry;
	protected final NativeComponentBinaries componentBinaries;

	public BaseNativeComponent(ComponentIdentifier<?> identifier, NamingScheme names, Class<T> variantType, ObjectFactory objects, ProviderFactory providers, TaskContainer tasks, ProjectLayout layout, ConfigurationContainer configurations) {
		super(identifier, names, variantType, objects);
		this.providers = providers;
		this.layout = layout;
		this.configurations = configurations;
		Preconditions.checkArgument(BaseNativeVariant.class.isAssignableFrom(variantType));
		this.variantType = variantType;
		getDevelopmentVariant().convention(providers.provider(new BuildableDevelopmentVariantConvention<>(getVariantCollection()::get)));
		this.taskRegistry = new TaskRegistryImpl(tasks);
		this.componentBinaries = new NativeComponentBinaries(taskRegistry, this, objects);
		((BinaryViewImpl<?>)getBinaries()).onRealize(() -> getVariantCollection().realize());
	}

	public abstract NativeComponentDependencies getDependencies();

	public VariantView<T> getVariants() {
		return getVariantCollection().getAsView(variantType);
	}

	public BinaryView<Binary> getBinaries() {
		return componentBinaries.getAsView();
	}

	public static <T> T one(Iterable<T> c) {
		Iterator<T> iterator = c.iterator();
		Preconditions.checkArgument(iterator.hasNext(), "collection needs to have one element, was empty");
		T result = iterator.next();
		Preconditions.checkArgument(!iterator.hasNext(), "collection needs to only have one element, more than one element found");
		return result;
	}

	protected abstract T createVariant(VariantIdentifier<?> identifier, VariantComponentDependencies<?> dependencies);

	protected abstract VariantComponentDependencies<?> newDependencies(NamingScheme names, BuildVariantInternal buildVariant);

	@Override
	public NativeComponentBinaries getComponentBinaries() {
		return componentBinaries;
	}

	protected void createBinaries(KnownVariant<T> knownVariant) {
		componentBinaries.createBinaries(knownVariant);

		knownVariant.configure(it -> {
			val incomingDependencies = (NativeIncomingDependencies) it.getResolvableDependencies();
			getBinaries().withType(ExecutableBinaryInternal.class).configureEach(binary -> {
				binary.getLinkTask().configure(task -> {
					((LinkExecutableTask)task).getLibs().from(incomingDependencies.getLinkLibraries());
					((LinkExecutableTask)task).getLinkerArgs().addAll(getProviders().provider(() -> incomingDependencies.getLinkFrameworks().getFiles().stream().flatMap(ExecutableBinaryInternal::toFrameworkFlags).collect(Collectors.toList())));
				});
			});
			getBinaries().withType(SharedLibraryBinaryInternal.class).configureEach(binary -> {
				binary.getLinkTask().configure(task -> {
					((LinkSharedLibraryTask)task).getLibs().from(incomingDependencies.getLinkLibraries());
					((LinkSharedLibraryTask)task).getLinkerArgs().addAll(getProviders().provider(() -> incomingDependencies.getLinkFrameworks().getFiles().stream().flatMap(SharedLibraryBinaryInternal::toFrameworkFlags).collect(Collectors.toList())));
				});
			});
			getBinaries().withType(BaseNativeBinary.class).configureEach(binary -> {
				binary.getDependencies().set(incomingDependencies);
				binary.getCompileTasks().configureEach(AbstractNativeCompileTask.class::isInstance, task -> {
					((AbstractNativeCompileTask)task).getIncludes().from(incomingDependencies.getHeaderSearchPaths());
					((AbstractNativeCompileTask)task).getCompilerArgs().addAll(getProviders().provider(() -> incomingDependencies.getFrameworkSearchPaths().getFiles().stream().flatMap(BaseNativeBinary::toFrameworkSearchPathFlags).collect(Collectors.toList())));
				});
				binary.getCompileTasks().configureEach(SwiftCompileTask.class, task -> {
					task.getModules().from(incomingDependencies.getSwiftModules());
					task.getCompilerArgs().addAll(getProviders().provider(() -> incomingDependencies.getFrameworkSearchPaths().getFiles().stream().flatMap(BaseNativeBinary::toFrameworkSearchPathFlags).collect(Collectors.toList())));
				});
				binary.getBaseName().convention(getBaseName());

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

	protected void calculateVariants() {
		getBuildVariants().get().forEach(buildVariant -> {
			final NamingScheme names = this.getNames().forBuildVariant(buildVariant, getBuildVariants().get());
			final VariantIdentifier<T> variantIdentifier = VariantIdentifier.builder().withUnambiguousNameFromBuildVariants(buildVariant, getBuildVariants().get()).withComponentIdentifier(getIdentifier()).withType(variantType).build();

			val dependencies = newDependencies(names.withComponentDisplayName(getIdentifier().getDisplayName()), buildVariant);
			VariantProvider<T> variant = getVariantCollection().registerVariant(variantIdentifier, (name, bv) -> createVariant(variantIdentifier, dependencies));

			onEachVariantDependencies(variant, dependencies);
		});
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
}
