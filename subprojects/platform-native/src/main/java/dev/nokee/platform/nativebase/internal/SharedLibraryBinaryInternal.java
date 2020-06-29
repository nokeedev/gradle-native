package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.internal.DefaultHeaderSearchPath;
import dev.nokee.language.nativebase.internal.HeaderExportingSourceSetInternal;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Buildable;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.Swiftc;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SharedLibraryBinaryInternal extends BaseNativeBinary implements SharedLibraryBinary, Buildable {
	private final TaskProvider<LinkSharedLibraryTask> linkTask;
	private final NativeIncomingDependencies dependencies;
	private final DomainObjectSet<? super LanguageSourceSetInternal> sources;

	// TODO: The dependencies passed over here should be a read-only like only FileCollections
	@Inject
	public SharedLibraryBinaryInternal(NamingScheme names, DomainObjectSet<LanguageSourceSetInternal> parentSources, DefaultTargetMachine targetMachine, DomainObjectSet<GeneratedSourceSet> objectSourceSets, TaskProvider<LinkSharedLibraryTask> linkTask, NativeIncomingDependencies dependencies) {
		super(names, objectSourceSets, targetMachine, dependencies);
		this.linkTask = linkTask;
		this.dependencies = dependencies;
		sources = getObjects().domainObjectSet(LanguageSourceSetInternal.class);
		parentSources.all(it -> sources.add(it));

		// configure includes using the native incoming compile configuration
		compileTasks.configureEach(AbstractNativeCompileTask.class, task -> {
			AbstractNativeCompileTask softwareModelTaskInternal = (AbstractNativeCompileTask) task;
			NativeSourceCompileTask taskInternal = (NativeSourceCompileTask) task;
			taskInternal.getHeaderSearchPaths().addAll(softwareModelTaskInternal.getIncludes().getElements().map(SharedLibraryBinaryInternal::toHeaderSearchPaths));

			sources.withType(HeaderExportingSourceSetInternal.class, sourceSet -> softwareModelTaskInternal.getIncludes().from(sourceSet.getSource()));

			task.getIncludes().from("src/main/public");

			// TODO: Move this to JNI Library configuration
			softwareModelTaskInternal.getIncludes().from(getJvmIncludes());
		});

		linkTask.configure(task -> {
			task.getLibs().from(dependencies.getLinkLibraries());
			task.getLinkerArgs().addAll(getProviders().provider(() -> dependencies.getLinkFrameworks().getFiles().stream().flatMap(this::toFrameworkFlags).collect(Collectors.toList())));

			task.getLinkerArgs().addAll(task.getToolChain().map(it -> {
				if (it instanceof Swiftc && targetMachine.getOperatingSystemFamily().isMacOs()) {
					// TODO: Support DEVELOPER_DIR or request the xcrun tool from backend
					return ImmutableList.of("-sdk", CommandLine.of("xcrun", "--show-sdk-path").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().getAsString().trim());
				}
				return ImmutableList.of();
			}));

			task.getLinkerArgs().addAll(task.getToolChain().map(it -> {
				if (it instanceof Swiftc) {
					return ImmutableList.of("-module-name", toModuleName(getBaseName().get()));
				}
				return ImmutableList.of();
			}));
		});
		linkTask.configure(this::configureSharedLibraryTask);

		getLinkedFile().set(linkTask.flatMap(AbstractLinkTask::getLinkedFile));
		getLinkedFile().disallowChanges();
	}

	private String toModuleName(String baseName) {
		return GUtil.toCamelCase(baseName);
	}

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	protected abstract ObjectFactory getObjects();

	private void configureSharedLibraryTask(LinkSharedLibraryTask task) {
		task.setDescription("Links the shared library.");
		task.source(getObjectFiles());

		task.getTargetPlatform().set(getTargetPlatform());
		task.getTargetPlatform().finalizeValueOnRead();
		task.getTargetPlatform().disallowChanges();

		// Until we model the build type
		task.getDebuggable().set(false);

		Provider<String> installName = task.getLinkedFile().getLocationOnly().map(linkedFile -> linkedFile.getAsFile().getName());
		task.getInstallName().set(installName);

		task.getDestinationDirectory().convention(getLayout().getBuildDirectory().dir(getNames().getOutputDirectoryBase("libs")));
		task.getLinkedFile().convention(getSharedLibraryLinkedFile());

		// For windows
		task.getImportLibrary().convention(getImportLibraryFile(task.getToolChain().map(selectToolProvider(getTargetMachine()))));

		task.getToolChain().set(selectNativeToolChain(getTargetMachine()));
		task.getToolChain().finalizeValueOnRead();
		task.getToolChain().disallowChanges();
	}

	private Transformer<PlatformToolProvider, NativeToolChain> selectToolProvider(TargetMachine targetMachine) {
		return toolChain -> {
			NativeToolChainInternal toolChainInternal = (NativeToolChainInternal) toolChain;
			return toolChainInternal.select(NativePlatformFactory.create(targetMachine));
		};
	}

	private Provider<RegularFile> getImportLibraryFile(Provider<PlatformToolProvider> platformToolProvider) {
		return getProviders().provider(() -> {
			PlatformToolProvider toolProvider = platformToolProvider.get();
			if (toolProvider.producesImportLibrary()) {
				return getLayout().getBuildDirectory().file(toolProvider.getImportLibraryName(getNames().getOutputDirectoryBase("libs") + "/" + getBaseName().get())).get();
			}
			return null;
		});
	}

	private Provider<RegularFile> getSharedLibraryLinkedFile() {
		return getLayout().getBuildDirectory().file(getBaseName().map(it -> {
			OperatingSystemFamily osFamily = getTargetMachine().getOperatingSystemFamily();
			OperatingSystemOperations osOperations = OperatingSystemOperations.of(osFamily);
			return osOperations.getSharedLibraryName(getNames().getOutputDirectoryBase("libs") + "/" + it);
		}));
	}

	public TaskProvider<LinkSharedLibrary> getLinkTask() {
		return getTasks().named(linkTask.getName(), LinkSharedLibrary.class);
	}

	@Inject
	protected abstract ProviderFactory getProviderFactory();

	@Inject
	protected abstract TaskContainer getTasks();

	public abstract RegularFileProperty getLinkedFile();

	@Override
	public boolean isBuildable() {
		return super.isBuildable() && isBuildable(linkTask.get());
	}

	private static boolean isBuildable(LinkSharedLibrary linkTask) {
		AbstractLinkTask linkTaskInternal = (AbstractLinkTask)linkTask;
		return isBuildable(linkTaskInternal.getToolChain().get(), linkTaskInternal.getTargetPlatform().get());
	}

	private static List<HeaderSearchPath> toHeaderSearchPaths(Set<FileSystemLocation> paths) {
		return paths.stream().map(FileSystemLocation::getAsFile).map(DefaultHeaderSearchPath::new).collect(Collectors.toList());
	}

	private Provider<List<File>> getJvmIncludes() {
		return getProviderFactory().provider(() -> {
			List<File> result = new ArrayList<>();
			result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include"));

			if (OperatingSystem.current().isMacOsX()) {
				result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/darwin"));
			} else if (OperatingSystem.current().isLinux()) {
				result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/linux"));
			} else if (OperatingSystem.current().isWindows()) {
				result.add(new File(Jvm.current().getJavaHome().getAbsolutePath() + "/include/win32"));
			}
			return result;
		});
	}

	private Stream<String> toFrameworkFlags(File it) {
		return ImmutableList.of("-F", it.getParent(), "-framework", FilenameUtils.removeExtension(it.getName())).stream();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return task -> ImmutableSet.of(getLinkTask().get());
	}

	public FileCollection getRuntimeLibrariesDependencies() {
		return dependencies.getRuntimeLibraries();
	}
}
