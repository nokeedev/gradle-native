package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.language.nativebase.internal.UTTypeObjectCode;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.BinaryInternal;
import dev.nokee.platform.base.internal.DefaultTaskView;
import dev.nokee.platform.base.internal.Realizable;
import dev.nokee.platform.nativebase.NativeBinary;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;

import javax.inject.Inject;
import java.util.stream.Collectors;

public abstract class BaseNativeBinary extends BinaryInternal implements NativeBinary {
	private final DefaultTaskView<NativeSourceCompile> compileTasks;

	public BaseNativeBinary() {
		compileTasks = null;
	}

	public BaseNativeBinary(DomainObjectSet<GeneratedSourceSet<UTTypeObjectCode>> objectSourceSets) {
		this.compileTasks = getObjects().newInstance(DefaultTaskView.class, objectSourceSets.stream().map(GeneratedSourceSet::getGeneratedByTask).collect(Collectors.toList()), (Realizable)() -> {});
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public TaskView<? extends NativeSourceCompile> getCompileTasks() {
		return compileTasks;
	}

	@Override
	public boolean isBuildable() {
		if (!compileTasks.getElements().get().stream().allMatch(BaseNativeBinary::isBuildable)) {
			return false;
		}
		return true;
	}

	private static boolean isBuildable(NativeSourceCompile compileTask) {
		AbstractNativeCompileTask compileTaskInternal = (AbstractNativeCompileTask)compileTask;
		return isBuildable(compileTaskInternal.getToolChain().get(), compileTaskInternal.getTargetPlatform().get());
	}

	protected static boolean isBuildable(NativeToolChain toolchain, NativePlatform platform) {
		NativeToolChainInternal toolchainInternal = (NativeToolChainInternal)toolchain;
		NativePlatformInternal platformInternal = (NativePlatformInternal)platform;
		PlatformToolProvider toolProvider = toolchainInternal.select(platformInternal);
		return toolProvider.isAvailable();
	}

	public abstract Property<String> getBaseName();
}
