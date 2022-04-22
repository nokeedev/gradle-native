/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.internal.SourceFiles;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.language.c.tasks.CCompile;
import org.gradle.language.cpp.tasks.CppCompile;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.objectivec.tasks.ObjectiveCCompile;
import org.gradle.language.objectivecpp.tasks.ObjectiveCppCompile;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.ToolType;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;

public final class NativeCompileTaskDefaultConfigurationRule extends ModelActionWithInputs.ModelAction2<NativeCompileTask, SourceFiles> {
	private final ModelRegistry registry;

	public NativeCompileTaskDefaultConfigurationRule(ModelRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, NativeCompileTask compileTask, SourceFiles sourceFiles) {
		registry.instantiate(configure(compileTask.get().getId(), NativeSourceCompileTask.class, configurePositionIndependentCode(set(true))));
		registry.instantiate(configure(compileTask.get().getId(), NativeSourceCompileTask.class, configureSystemIncludes(from(platformTool()))));
		registry.instantiate(configure(compileTask.get().getId(), NativeSourceCompileTask.class, configureSources(from(sourceFiles))));
	}

	//region Position independent code
	private static Action<NativeSourceCompileTask> configurePositionIndependentCode(BiConsumer<? super NativeSourceCompileTask, ? super PropertyUtils.SetAwareProperty<Boolean>> action) {
		return task -> action.accept(task, wrap(((AbstractNativeCompileTask) task)::setPositionIndependentCode));
	}
	//endregion

	//region System includes
	private static Action<NativeSourceCompileTask> configureSystemIncludes(BiConsumer<? super AbstractNativeCompileTask, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> action.accept((AbstractNativeCompileTask) task, wrap(((AbstractNativeCompileTask) task).getSystemIncludes()));
	}

	private static Function<AbstractNativeCompileTask, Object> platformTool() {
		return compileTask -> fromPlatformToolOf(compileTask);
	}

	private static Callable<List<File>> fromPlatformToolOf(AbstractNativeCompileTask compileTask) {
		return () -> {
			NativeToolChainInternal toolChain = (NativeToolChainInternal)compileTask.getToolChain().get();
			NativePlatformInternal targetPlatform = (NativePlatformInternal)compileTask.getTargetPlatform().get();
			PlatformToolProvider toolProvider = toolChain.select(targetPlatform);

			return toolProvider.getSystemLibraries(toolType(compileTask.getClass())).getIncludeDirs();
		};
	}

	private static ToolType toolType(Class<? extends Task> taskType) {
		if (CCompile.class.isAssignableFrom(taskType)) {
			return ToolType.CPP_COMPILER;
		} else if (CppCompile.class.isAssignableFrom(taskType)) {
			return ToolType.CPP_COMPILER;
		} else if (ObjectiveCCompile.class.isAssignableFrom(taskType)) {
			return ToolType.OBJECTIVEC_COMPILER;
		} else if (ObjectiveCppCompile.class.isAssignableFrom(taskType)) {
			return ToolType.OBJECTIVECPP_COMPILER;
		} else {
			throw new IllegalArgumentException(String.format("Unknown task type, '%s', cannot choose ToolType.", taskType.getSimpleName()));
		}
	}
	//endregion

	//region Task sources
	public static Action<NativeSourceCompileTask> configureSources(BiConsumer<? super NativeSourceCompileTask, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> action.accept(task, wrap(task.getSource()));
	}
	//endregion
}
