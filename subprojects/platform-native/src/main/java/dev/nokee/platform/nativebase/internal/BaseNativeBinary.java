package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.c.tasks.CCompile;
import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import dev.nokee.language.objectivec.tasks.ObjectiveCCompile;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.language.objectivecpp.tasks.ObjectiveCppCompile;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.DefaultTaskView;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.Realizable;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.Getter;
import lombok.val;
import lombok.var;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.language.swift.SwiftVersion;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.ToolType;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BaseNativeBinary implements Binary, NativeBinary {
	private final ToolChainSelectorInternal toolChainSelector = getObjects().newInstance(ToolChainSelectorInternal.class);
	@Getter private final NamingScheme names;
	protected final TaskView<Task> compileTasks; // Until the compile tasks is clean up
	private final DomainObjectSet<GeneratedSourceSet> objectSourceSets;
	@Getter private final DefaultTargetMachine targetMachine;
	@Getter private final NativeIncomingDependencies dependencies;

	public BaseNativeBinary(NamingScheme names, DomainObjectSet<GeneratedSourceSet> objectSourceSets, DefaultTargetMachine targetMachine, NativeIncomingDependencies dependencies) {
		this.names = names;
		this.compileTasks = getObjects().newInstance(DefaultTaskView.class, Task.class, objectSourceSets.stream().map(GeneratedSourceSet::getGeneratedByTask).collect(Collectors.toList()), (Realizable)() -> {});
		this.objectSourceSets = objectSourceSets;
		this.targetMachine = targetMachine;
		this.dependencies = dependencies;

		compileTasks.configureEach(AbstractNativeCompileTask.class, this::configureNativeSourceCompileTask);
		compileTasks.configureEach(AbstractNativeCompileTask.class, task -> {
			task.getIncludes().from(dependencies.getHeaderSearchPaths());
			task.getCompilerArgs().addAll(getProviders().provider(() -> dependencies.getFrameworkSearchPaths().getFiles().stream().flatMap(this::toFrameworkSearchPathFlags).collect(Collectors.toList())));
		});
		compileTasks.configureEach(SwiftCompileTask.class, this::configureSwiftCompileTask);
		compileTasks.configureEach(SwiftCompileTask.class, task -> {
			task.getModules().from(dependencies.getSwiftModules());
			task.getCompilerArgs().addAll(getProviders().provider(() -> dependencies.getFrameworkSearchPaths().getFiles().stream().flatMap(this::toFrameworkSearchPathFlags).collect(Collectors.toList())));
		});
	}

	public Provider<Set<FileSystemLocation>> getHeaderSearchPaths() {
		return getObjects().fileCollection()
			.from("src/main/headers")
			.from(compileTasks.withType(AbstractNativeSourceCompileTask.class).map(it -> it.getIncludes()))
			.from(getDependencies().getHeaderSearchPaths())
			.from(compileTasks.withType(AbstractNativeSourceCompileTask.class).map(it -> it.getSystemIncludes()))
			.getElements();
	}

	public Provider<Set<FileSystemLocation>> getImportSearchPaths() {
		return getObjects().fileCollection()
			.from(getCompileTasks().withType(SwiftCompileTask.class).getElements().map(tasks -> tasks.stream().map(task -> task.getModuleFile().map(it -> it.getAsFile().getParentFile())).collect(Collectors.toList())))
			.from(getDependencies().getSwiftModules().getElements().map(files -> files.stream().map(it -> it.getAsFile().getParentFile()).collect(Collectors.toList())))
			.getElements();
	}

	public Provider<Set<FileSystemLocation>> getFrameworkSearchPaths() {
		return getObjects().fileCollection()
			.from(getDependencies().getFrameworkSearchPaths())
			.from(getDependencies().getLinkFrameworks().getElements().map(files -> files.stream().map(it -> it.getAsFile().getParentFile()).collect(Collectors.toList())))
			.from(compileTasks.withType(AbstractNativeSourceCompileTask.class).map(it -> extractFrameworkSearchPaths(it.getCompilerArgs().get())))
			.getElements();
	}

	private static List<File> extractFrameworkSearchPaths(List<String> args) {
		val result = new ArrayList<File>();
		var nextArgIsFrameworkSearchPath = false;
		for (String arg : args) {
			if (nextArgIsFrameworkSearchPath) {
				result.add(new File(arg));
				nextArgIsFrameworkSearchPath = false;
			} else if (arg.equals("-F")) {
				nextArgIsFrameworkSearchPath = true;
			}
		}
		return result;
	}

	private void configureNativeSourceCompileTask(AbstractNativeCompileTask task) {
		task.getObjectFileDir().convention(languageNameSuffixFor(task).flatMap(languageNameSuffix -> getLayout().getBuildDirectory().dir(names.getOutputDirectoryBase("objs") + "/main" + languageNameSuffix)));

		task.getTargetPlatform().set(getTargetPlatform());
		task.getTargetPlatform().finalizeValueOnRead();
		task.getTargetPlatform().disallowChanges();

		// TODO: Select the right value based on the build type dimension, once modeled
		task.setDebuggable(false);
		task.setOptimized(false);
		task.setPositionIndependentCode(true);

		task.getToolChain().set(selectNativeToolChain(targetMachine));
		task.getToolChain().finalizeValueOnRead();
		task.getToolChain().disallowChanges();

		task.getIncludes().from("src/main/headers");

		task.getSystemIncludes().from(getSystemIncludes(task));
	}

	private Provider<String> languageNameSuffixFor(AbstractNativeCompileTask task) {
		return getProviders().provider(() -> {
			if (task instanceof CCompile) {
				return "C";
			} else if (task instanceof CppCompile) {
				return "Cpp";
			} else if (task instanceof ObjectiveCCompile) {
				return "ObjectiveC";
			} else if (task instanceof ObjectiveCppCompile) {
				return "ObjectiveCpp";
			}
			throw new IllegalArgumentException(String.format("Unknown native compile task '%s' (%s).", task.getName(), task.getClass().getSimpleName()));
		});

	}

	private void configureSwiftCompileTask(SwiftCompileTask task) {
		task.getObjectFileDir().convention(getLayout().getBuildDirectory().dir(names.getOutputDirectoryBase("objs") + "/mainSwift"));

		// TODO: Select the right value based on the build type dimension, once modeled
		task.getDebuggable().set(false);
		task.getOptimized().set(false);
		task.getSourceCompatibility().set(SwiftVersion.SWIFT5);

		task.getTargetPlatform().set(getTargetPlatform());
		task.getTargetPlatform().finalizeValueOnRead();
		task.getTargetPlatform().disallowChanges();

		task.getToolChain().set(selectSwiftToolChain(targetMachine));
		task.getToolChain().finalizeValueOnRead();
		task.getToolChain().disallowChanges();

		task.getModuleName().convention(getBaseName().map(this::toModuleName));
		task.getModuleFile().convention(task.getModuleName().flatMap(this::toSwiftModuleFile));

		if (targetMachine.getOperatingSystemFamily().isMacOs()) {
			task.getCompilerArgs().add("-sdk");
			// TODO: Support DEVELOPER_DIR or request the xcrun tool from backend
			task.getCompilerArgs().add(getProviders().provider(() -> CommandLine.of("xcrun", "--show-sdk-path").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().getAsString().trim()));
		}

		if (this instanceof SharedLibraryBinary || this instanceof StaticLibraryBinary) {
			task.getCompilerArgs().add("-parse-as-library");
		}
	}

	private String toModuleName(String baseName) {
		return GUtil.toCamelCase(baseName);
	}

	private Provider<RegularFile> toSwiftModuleFile(String moduleName) {
		return getLayout().getBuildDirectory().file("modules/main/" + moduleName + ".swiftmodule");
	}

	Provider<NativeToolChain> selectNativeToolChain(TargetMachine targetMachine) {
		return getProviders().provider(() -> toolChainSelector.select(targetMachine));
	}

	private Provider<NativeToolChain> selectSwiftToolChain(TargetMachine targetMachine) {
		return getProviders().provider(() -> toolChainSelector.selectSwift(targetMachine));
	}

	Provider<NativePlatform> getTargetPlatform() {
		return getProviders().provider(() -> NativePlatformFactory.create(targetMachine));
	}

	private ToolType getToolType(Class<? extends Task> taskType) {
		if (CCompileTask.class.isAssignableFrom(taskType)) {
			return ToolType.CPP_COMPILER;
		} else if (CppCompileTask.class.isAssignableFrom(taskType)) {
			return ToolType.CPP_COMPILER;
		} else if (ObjectiveCCompileTask.class.isAssignableFrom(taskType)) {
			return ToolType.OBJECTIVEC_COMPILER;
		} else if (ObjectiveCppCompileTask.class.isAssignableFrom(taskType)) {
			return ToolType.OBJECTIVECPP_COMPILER;
		}
		throw new IllegalArgumentException(String.format("Unknown task type, '%s', cannot choose ToolType.", taskType.getSimpleName()));
	}

	private Callable<List<File>> getSystemIncludes(AbstractNativeCompileTask compileTask) {
		return () -> {
			NativeToolChainInternal toolChain = (NativeToolChainInternal)compileTask.getToolChain().get();
			NativePlatformInternal targetPlatform = (NativePlatformInternal)compileTask.getTargetPlatform().get();
			PlatformToolProvider toolProvider = toolChain.select(targetPlatform);

			return toolProvider.getSystemLibraries(getToolType(compileTask.getClass())).getIncludeDirs();
		};
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	protected abstract ProviderFactory getProviders();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Override
	public boolean isBuildable() {
		if (!compileTasks.withType(AbstractNativeCompileTask.class).get().stream().allMatch(BaseNativeBinary::isBuildable)) {
			return false;
		}
		if (!compileTasks.withType(SwiftCompileTask.class).get().stream().allMatch(BaseNativeBinary::isBuildable)) {
			return false;
		}
		return true;
	}

	private static boolean isBuildable(AbstractNativeCompileTask compileTask) {
		return isBuildable(compileTask.getToolChain().get(), compileTask.getTargetPlatform().get());
	}

	private static boolean isBuildable(SwiftCompileTask compileTask) {
		return isBuildable(compileTask.getToolChain().get(), compileTask.getTargetPlatform().get());
	}

	protected static boolean isBuildable(NativeToolChain toolchain, NativePlatform platform) {
		NativeToolChainInternal toolchainInternal = (NativeToolChainInternal)toolchain;
		NativePlatformInternal platformInternal = (NativePlatformInternal)platform;
		PlatformToolProvider toolProvider = toolchainInternal.select(platformInternal);
		return toolProvider.isAvailable();
	}

	public abstract Property<String> getBaseName();

	public Object getObjectFiles() {
		Optional<Object> result = objectSourceSets.stream().map(GeneratedSourceSet::getAsFileTree).reduce(FileTree::plus).map(it -> it);
		return result.orElseGet(() -> ImmutableList.of());
	}

	private Stream<String> toFrameworkSearchPathFlags(File it) {
		return ImmutableList.of("-F", it.getAbsolutePath()).stream();
	}

	@Override
	public TaskView<SourceCompile> getCompileTasks() {
		return compileTasks.withType(SourceCompile.class);
	}
}
