package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.internal.DefaultHeaderSearchPath;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.platform.nativebase.tasks.internal.ObjectFilesToBinaryTask;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Buildable;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.*;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.Swiftc;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SharedLibraryBinaryInternal extends BaseNativeBinary implements SharedLibraryBinary, Buildable {
	private final TaskProvider<LinkSharedLibraryTask> linkTask;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providerFactory;
	@Getter(AccessLevel.PROTECTED) private final ConfigurationContainer configurations;
	@Getter(AccessLevel.PROTECTED) private final TaskContainer tasks;
	@Getter RegularFileProperty linkedFile;

	// TODO: The dependencies passed over here should be a read-only like only FileCollections
	@Inject
	public SharedLibraryBinaryInternal(NamingScheme names, DomainObjectSet<LanguageSourceSetInternal> parentSources, DefaultTargetMachine targetMachine, DomainObjectSet<GeneratedSourceSet> objectSourceSets, TaskProvider<LinkSharedLibraryTask> linkTask, ObjectFactory objects, ProjectLayout layout, ProviderFactory providers, ConfigurationContainer configurations, TaskContainer tasks) {
		super(names, objectSourceSets, targetMachine, objects, layout, providers, configurations);
		this.linkTask = linkTask;
		this.objects = objects;
		this.providerFactory = providers;
		this.configurations = configurations;
		this.tasks = tasks;
		this.linkedFile = objects.fileProperty();

		// configure includes using the native incoming compile configuration
		compileTasks.configureEach(AbstractNativeCompileTask.class, task -> {
			NativeSourceCompileTask taskInternal = (NativeSourceCompileTask) task;
			taskInternal.getHeaderSearchPaths().addAll(task.getIncludes().getElements().map(SharedLibraryBinaryInternal::toHeaderSearchPaths));
		});

		linkTask.configure(task -> {
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

	@Override
	public TaskProvider<LinkSharedLibrary> getLinkTask() {
		return getTasks().named(linkTask.getName(), LinkSharedLibrary.class);
	}

	@Override
	public TaskProvider<ObjectFilesToBinaryTask> getCreateOrLinkTask() {
		return getTasks().named(linkTask.getName(), ObjectFilesToBinaryTask.class);
	}

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

	public static Stream<String> toFrameworkFlags(File it) {
		return ImmutableList.of("-F", it.getParent(), "-framework", FilenameUtils.removeExtension(it.getName())).stream();
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return task -> ImmutableSet.of(getLinkTask().get());
	}

	public FileCollection getRuntimeLibrariesDependencies() {
		return objects.fileCollection().from(getDependencies().map(NativeIncomingDependencies::getRuntimeLibraries));
	}
}
