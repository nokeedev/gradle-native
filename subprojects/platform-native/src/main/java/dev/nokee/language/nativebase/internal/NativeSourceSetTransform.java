package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.language.base.internal.SourceSetTransform;
import dev.nokee.language.base.internal.UTType;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.internal.DefaultTargetMachine;
import dev.nokee.platform.nativebase.internal.NativePlatformFactory;
import dev.nokee.platform.nativebase.internal.ToolChainSelectorInternal;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.Cast;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.ToolType;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class NativeSourceSetTransform<T extends UTType> implements SourceSetTransform<T, UTTypeObjectCode> {
	private final NamingScheme names;
	private final DefaultTargetMachine targetMachine;
	private final ToolChainSelectorInternal toolChainSelector;

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract TaskContainer getTasks();

	@Inject
	protected abstract ProjectLayout getLayout();

	@Inject
	public NativeSourceSetTransform(NamingScheme names, DefaultTargetMachine targetMachine, ToolChainSelectorInternal toolChainSelector) {
		this.names = names;
		this.targetMachine = targetMachine;
		this.toolChainSelector = toolChainSelector;
	}

	protected abstract Class<? extends AbstractNativeSourceCompileTask> getCompileTaskType();

	protected abstract String getLanguageName();

	protected abstract ToolType getToolType();

	@Override
	public SourceSet<UTTypeObjectCode> transform(SourceSet<T> sourceSet) {
		TaskProvider<? extends AbstractNativeSourceCompileTask> compileTask = getTasks().register(names.getTaskName("compile", getLanguageName()), getCompileTaskType(), task -> {
			task.getObjectFileDir().convention(getLayout().getBuildDirectory().dir(names.getOutputDirectoryBase("objs") + "/main" + getLanguageName()));

			// TODO: Select the right value based on the build type dimension, once modeled
			task.setDebuggable(false);
			task.setOptimized(false);

			NativePlatformFactory nativePlatformFactory = new NativePlatformFactory();
			NativePlatformInternal nativePlatform = nativePlatformFactory.create(targetMachine);
			task.getTargetPlatform().set(nativePlatform);
			task.getTargetPlatform().finalizeValueOnRead();
			task.getTargetPlatform().disallowChanges();

			task.getSource().from(sourceSet.getAsFileTree());

			NativeToolChainInternal toolChain = toolChainSelector.select(targetMachine);
			task.getToolChain().set(toolChain);
			task.getToolChain().finalizeValueOnRead();
			task.getToolChain().disallowChanges();

			final Callable<List<File>> systemIncludes = () -> toolChain.select(nativePlatform).getSystemLibraries(getToolType()).getIncludeDirs();
			task.getSystemIncludes().from(systemIncludes);
		});

		return Cast.uncheckedCast(getObjects().newInstance(GeneratedSourceSet.class, new UTTypeObjectCode(), compileTask.flatMap(AbstractNativeSourceCompileTask::getObjectFileDir), compileTask));
	}
}
