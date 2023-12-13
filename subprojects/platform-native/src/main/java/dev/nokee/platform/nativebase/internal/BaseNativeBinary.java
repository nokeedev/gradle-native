/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.BuildableComponentSpec;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.utils.TaskDependencyUtils;
import lombok.val;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static dev.nokee.util.ProviderOfIterableTransformer.toProviderOfIterable;
import static dev.nokee.utils.TransformerUtils.transformEach;

public abstract class BaseNativeBinary extends ModelElementSupport implements Binary, NativeBinary
	, HasHeaderSearchPaths
	, HasObjectFilesToBinaryTask
	, BuildableComponentSpec
{
	private final ObjectFactory objects;
	private final ProviderFactory providers;

	public BaseNativeBinary(ObjectFactory objects, ProviderFactory providers) {
		this.objects = objects;
		this.providers = providers;
		getBuildDependencies().add(TaskDependencyUtils.of(getCreateOrLinkTask()));
	}

	public Provider<Set<FileSystemLocation>> getHeaderSearchPaths() {
		return objects.fileCollection()
			.from(getCompileTasks().filter(AbstractNativeSourceCompileTask.class::isInstance).map(transformEach(it -> ((AbstractNativeSourceCompileTask) it).getIncludes().getElements())).flatMap(toProviderOfIterable(objects::listProperty)))
			.from(getCompileTasks().filter(AbstractNativeSourceCompileTask.class::isInstance).map(transformEach(it -> ((AbstractNativeSourceCompileTask) it).getSystemIncludes().getElements())).flatMap(toProviderOfIterable(objects::listProperty)))
			.getElements();
	}

	public Provider<Set<FileSystemLocation>> getImportSearchPaths() {
		return objects.fileCollection()
			.from(getCompileTasks().flatMap(it -> {
				if (it instanceof SwiftCompile) {
					return Collections.singletonList((SwiftCompile) it);
				} else {
					return Collections.emptyList();
				}
			}).map(transformEach(task -> task.getModuleFile().map(it -> it.getAsFile().getParentFile()))).flatMap(toProviderOfIterable(objects::listProperty)))
			.getElements();
	}

	public Provider<Set<FileSystemLocation>> getFrameworkSearchPaths() {
		return objects.fileCollection()
			.from(getCompileTasks().filter(AbstractNativeSourceCompileTask.class::isInstance).map(transformEach(it -> extractFrameworkSearchPaths(((AbstractNativeSourceCompileTask) it).getCompilerArgs().get()))).flatMap(it -> providers.provider(() -> it)))
			.getElements();
	}

	private static List<File> extractFrameworkSearchPaths(List<String> args) {
		val result = new ArrayList<File>();
		boolean nextArgIsFrameworkSearchPath = false;
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

	@Override
	public boolean isBuildable() {
		try {
			if (!getCompileTasks().filter(AbstractNativeCompileTask.class::isInstance).get().stream().map(AbstractNativeCompileTask.class::cast).allMatch(BaseNativeBinary::isBuildable)) {
				return false;
			}
			if (!getCompileTasks().filter(SwiftCompile.class::isInstance).get().stream().map(SwiftCompile.class::cast).allMatch(BaseNativeBinary::isBuildable)) {
				return false;
			}
			return true;
		} catch (Throwable ex) { // because toolchain selection calls xcrun for macOS which doesn't exists on non-mac system
			return false;
		}
	}

	private static boolean isBuildable(AbstractNativeCompileTask compileTask) {
		return isBuildable(compileTask.getToolChain().get(), compileTask.getTargetPlatform().get());
	}

	private static boolean isBuildable(SwiftCompile compileTask) {
		return isBuildable(compileTask.getToolChain().get(), compileTask.getTargetPlatform().get());
	}

	protected static boolean isBuildable(NativeToolChain toolchain, NativePlatform platform) {
		NativeToolChainInternal toolchainInternal = (NativeToolChainInternal)toolchain;
		NativePlatformInternal platformInternal = (NativePlatformInternal)platform;
		PlatformToolProvider toolProvider = toolchainInternal.select(platformInternal);
		return toolProvider.isAvailable();
	}
}
