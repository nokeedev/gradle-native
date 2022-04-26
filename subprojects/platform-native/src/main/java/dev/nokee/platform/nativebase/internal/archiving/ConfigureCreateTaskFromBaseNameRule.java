/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.archiving;

import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.BaseNamePropertyComponent;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import dev.nokee.platform.nativebase.tasks.ObjectLink;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;

final class ConfigureCreateTaskFromBaseNameRule extends ModelActionWithInputs.ModelAction2<BaseNamePropertyComponent, NativeArchiveTask> {
	private final ModelRegistry registry;

	public ConfigureCreateTaskFromBaseNameRule(ModelRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, BaseNamePropertyComponent baseNameProperty, NativeArchiveTask createTask) {
		@SuppressWarnings("unchecked")
		val baseName = (Provider<String>) baseNameProperty.get().get(GradlePropertyComponent.class).get();
//		registry.instantiate(configure(createTask.get().getId(), ObjectLink.class, configureLinkerArgs(addAll(forSwiftModuleName(baseName)))));
		registry.instantiate(configure(createTask.get().getId(), CreateStaticLibrary.class, configureOutputFile(convention(asStaticLibraryFile(baseName)))));
	}

	//region Output file
	private static <T extends Task> Action<T> configureOutputFile(BiConsumer<? super T, ? super PropertyUtils.Property<RegularFile>> action) {
		return task -> action.accept(task, PropertyUtils.wrap(outputFileProperty(task)));
	}

	private static Function<CreateStaticLibrary, Object> asStaticLibraryFile(Provider<String> baseName) {
		return task -> toolChainProperty(task)
			.map(selectToolProvider(targetPlatformProperty(task)))
			.map(it -> fileNamer(it))
			.orElse(targetPlatformProperty(task).map(it -> fileNamer(it)))
			.flatMap(staticLibraryLinkedFile(task.getDestinationDirectory(), baseName));
	}

	private interface FileNamer {
		String getStaticLibraryName(String libraryPath);
	}

	private static FileNamer fileNamer(PlatformToolProvider toolProvider) {
		return new PlatformToolFileNamer(toolProvider);
	}

	private static FileNamer fileNamer(NativePlatform targetPlatform) {
		return new TargetPlatformFileNamer(targetPlatform);
	}

	private static final class PlatformToolFileNamer implements FileNamer {
		private final PlatformToolProvider toolProvider;

		private PlatformToolFileNamer(PlatformToolProvider toolProvider) {
			this.toolProvider = toolProvider;
		}

		@Override
		public String getStaticLibraryName(String libraryPath) {
			return toolProvider.getStaticLibraryName(libraryPath);
		}
	}

	private static final class TargetPlatformFileNamer implements FileNamer {
		private final NativePlatform targetPlatform;

		private TargetPlatformFileNamer(NativePlatform targetPlatform) {
			this.targetPlatform = targetPlatform;
		}

		@Override
		public String getStaticLibraryName(String libraryPath) {
			return ((NativePlatformInternal) targetPlatform).getOperatingSystem().getInternalOs().getStaticLibraryName(libraryPath);
		}
	}

	private static Transformer<Provider<RegularFile>, FileNamer> staticLibraryLinkedFile(Provider<Directory> destinationDirectory, Provider<String> baseName) {
		return toolProvider -> destinationDirectory.flatMap(dir -> dir.file(baseName.map(toolProvider::getStaticLibraryName)));
	}

	private static Transformer<PlatformToolProvider, NativeToolChain> selectToolProvider(Provider<NativePlatform> nativePlatform) {
		return toolChain -> {
			NativeToolChainInternal toolChainInternal = (NativeToolChainInternal) toolChain;
			return toolChainInternal.select((NativePlatformInternal) nativePlatform.get());
		};
	}

	private static RegularFileProperty outputFileProperty(Task task) {
		if (task instanceof CreateStaticLibraryTask) {
			return ((CreateStaticLibraryTask) task).getOutputFile();
		} else {
			throw new IllegalArgumentException();
		}
	}

	private static Property<NativePlatform> targetPlatformProperty(Task task) {
		if (task instanceof CreateStaticLibraryTask) {
			return ((CreateStaticLibraryTask) task).getTargetPlatform();
		} else {
			throw new IllegalArgumentException();
		}
	}

	private static Property<NativeToolChain> toolChainProperty(Task task) {
		if (task instanceof CreateStaticLibraryTask) {
			return ((CreateStaticLibraryTask) task).getToolChain();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion
}
