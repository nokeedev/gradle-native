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

import com.google.common.collect.ImmutableList;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.utils.Cast;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.HasMultipleValues;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static dev.nokee.utils.TransformerUtils.transformEach;

public abstract class BaseNativeBinary implements Binary, NativeBinary, HasHeaderSearchPaths, ModelNodeAware {
	private final ModelNode entity = ModelNodeContext.getCurrentModelNode();
	@Getter protected final BinaryIdentifier identifier;
	@Getter private final TargetMachine targetMachine;
	private final ObjectFactory objects;
	private final ProviderFactory providers;

	public BaseNativeBinary(BinaryIdentifier identifier, TargetMachine targetMachine, ObjectFactory objects, ProviderFactory providers) {
		this.identifier = identifier;
		this.targetMachine = targetMachine;
		this.objects = objects;
		this.providers = providers;
	}

	public Provider<Set<FileSystemLocation>> getHeaderSearchPaths() {
		return objects.fileCollection()
			.from(getCompileTasks().filter(AbstractNativeSourceCompileTask.class::isInstance).map(transformEach(it -> ((AbstractNativeSourceCompileTask) it).getIncludes().getElements())).flatMap(new ToProviderOfIterableTransformer<>(() -> Cast.uncheckedCastBecauseOfTypeErasure(objects.listProperty(FileSystemLocation.class)))))
			.from(getCompileTasks().filter(AbstractNativeSourceCompileTask.class::isInstance).map(transformEach(it -> ((AbstractNativeSourceCompileTask) it).getSystemIncludes().getElements())).flatMap(new ToProviderOfIterableTransformer<>(() -> Cast.uncheckedCastBecauseOfTypeErasure(objects.listProperty(FileSystemLocation.class)))))
			.getElements();
	}

	public static final class ToProviderOfIterableTransformer<T, C extends Provider<? extends Iterable<T>> & HasMultipleValues<T>> implements Transformer<Provider<? extends Iterable<T>>, Iterable<Provider<T>>> {
		private final Supplier<C> containerSupplier;

		public ToProviderOfIterableTransformer(Supplier<C> containerSupplier) {
			this.containerSupplier = containerSupplier;
		}

		@Override
		public Provider<? extends Iterable<T>> transform(Iterable<Provider<T>> providers) {
			final C container = containerSupplier.get();
			for (Provider<T> provider : providers) {
				((HasMultipleValues<T>) container).addAll(provider.map(this::ensureList));
			}
			return container;
		}

		@SuppressWarnings("unchecked")
		private <OUT, IN> Iterable<OUT> ensureList(IN g) {
			if (g instanceof Iterable) {
				return (Iterable<OUT>) g;
			} else {
				return (Iterable<OUT>) ImmutableList.of(g);
			}
		}
	}

	public Provider<Set<FileSystemLocation>> getImportSearchPaths() {
		return objects.fileCollection()
			.from(getCompileTasks().withType(SwiftCompileTask.class).map(task -> task.getModuleFile().map(it -> it.getAsFile().getParentFile())).flatMap(new ToProviderOfIterableTransformer<>(() -> Cast.uncheckedCastBecauseOfTypeErasure(objects.listProperty(File.class)))))
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
			if (!getCompileTasks().filter(SwiftCompileTask.class::isInstance).get().stream().map(SwiftCompileTask.class::cast).allMatch(BaseNativeBinary::isBuildable)) {
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

	private static boolean isBuildable(SwiftCompileTask compileTask) {
		return isBuildable(compileTask.getToolChain().get(), compileTask.getTargetPlatform().get());
	}

	protected static boolean isBuildable(NativeToolChain toolchain, NativePlatform platform) {
		NativeToolChainInternal toolchainInternal = (NativeToolChainInternal)toolchain;
		NativePlatformInternal platformInternal = (NativePlatformInternal)platform;
		PlatformToolProvider toolProvider = toolchainInternal.select(platformInternal);
		return toolProvider.isAvailable();
	}

	@Override
	@SuppressWarnings("unchecked")
	public TaskView<SourceCompile> getCompileTasks() {
		return ModelProperties.getProperty(this, "compileTasks").as(TaskView.class).get();
	}

	@Override
	public ModelNode getNode() {
		return entity;
	}
}
