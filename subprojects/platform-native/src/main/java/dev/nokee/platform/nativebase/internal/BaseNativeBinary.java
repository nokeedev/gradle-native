package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.cpp.internal.tasks.CppCompileTask;
import dev.nokee.language.objectivec.internal.tasks.ObjectiveCCompileTask;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.DefaultTaskView;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.Realizable;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import lombok.Getter;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Task;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.ToolType;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public abstract class BaseNativeBinary implements Binary, NativeBinary {
	private final ToolChainSelectorInternal toolChainSelector = getObjects().newInstance(ToolChainSelectorInternal.class);
	@Getter private final NamingScheme names;
	@Getter private final TaskView<Task> compileTasks;
	private final DomainObjectSet<GeneratedSourceSet> objectSourceSets;
	@Getter private final DefaultTargetMachine targetMachine;

	public BaseNativeBinary(NamingScheme names, DomainObjectSet<GeneratedSourceSet> objectSourceSets, DefaultTargetMachine targetMachine) {
		this.names = names;
		this.compileTasks = getObjects().newInstance(DefaultTaskView.class, Task.class, objectSourceSets.stream().map(GeneratedSourceSet::getGeneratedByTask).collect(Collectors.toList()), (Realizable)() -> {});
		this.objectSourceSets = objectSourceSets;
		this.targetMachine = targetMachine;

		getCompileTasks().configureEach(AbstractNativeCompileTask.class, this::configureNativeSourceCompileTask);
		getCompileTasks().configureEach(SwiftCompileTask.class, this::configureSwiftCompileTask);
	}

	private void configureNativeSourceCompileTask(AbstractNativeCompileTask task) {
		task.getTargetPlatform().set(getTargetPlatform());
		task.getTargetPlatform().finalizeValueOnRead();
		task.getTargetPlatform().disallowChanges();

		task.setPositionIndependentCode(this instanceof SharedLibraryBinary);

		task.getToolChain().set(selectNativeToolChain(targetMachine));
		task.getToolChain().finalizeValueOnRead();
		task.getToolChain().disallowChanges();

		task.getIncludes().from("src/main/headers");

		task.getSystemIncludes().from(getSystemIncludes(task));
	}

	private void configureSwiftCompileTask(SwiftCompileTask task) {
		task.getTargetPlatform().set(getTargetPlatform());
		task.getTargetPlatform().finalizeValueOnRead();
		task.getTargetPlatform().disallowChanges();

		task.getToolChain().set(selectSwiftToolChain(targetMachine));
		task.getToolChain().finalizeValueOnRead();
		task.getToolChain().disallowChanges();

		task.getModuleName().convention(getBaseName().map(this::toModuleName));
		task.getModuleFile().convention(task.getModuleName().flatMap(this::toSwiftModuleFile));
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

	public FileTree getObjectFiles() {
		Optional<FileTree> result = objectSourceSets.stream().map(GeneratedSourceSet::getAsFileTree).reduce(FileTree::plus);
		return result.orElseGet(() -> getObjects().fileTree());
	}
}
