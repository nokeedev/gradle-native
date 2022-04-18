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
package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.internal.BaseNamePropertyComponent;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.nativebase.tasks.ObjectLink;
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
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.Swiftc;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.util.GUtil;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static dev.nokee.platform.base.internal.util.PropertyUtils.addAll;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;

// ComponentFromEntity<GradlePropertyComponent>
public final class ConfigureLinkTaskFromBaseNameRule extends ModelActionWithInputs.ModelAction3<BinaryIdentifier<?>, BaseNamePropertyComponent, NativeLinkTask> {
	@Override
	protected void execute(ModelNode entity, BinaryIdentifier<?> objects, BaseNamePropertyComponent baseNameProperty, NativeLinkTask linkTask) {
		@SuppressWarnings("unchecked")
		val baseName = (Provider<String>) baseNameProperty.get().get(GradlePropertyComponent.class).get();
		linkTask.configure(ObjectLink.class, configureLinkerArgs(addAll(forSwiftModuleName(baseName))));
		linkTask.configure(ObjectLink.class, configureLinkedFile(convention(asSharedLibraryFile(baseName))));
	}

	//region Linker arguments
	private static Action<ObjectLink> configureLinkerArgs(BiConsumer<? super ObjectLink, ? super PropertyUtils.CollectionProperty<String>> action) {
		return task -> action.accept(task, wrap(task.getLinkerArgs()));
	}

	private static Function<ObjectLink, Object> forSwiftModuleName(Provider<String> baseName) {
		return task -> ((AbstractLinkTask) task).getToolChain().map(it -> {
			if (it instanceof Swiftc) {
				return ImmutableList.of("-module-name", baseName.map(toModuleName()).get());
			} else {
				return ImmutableList.of();
			}
		}).orElse(ImmutableList.of());
	}

	private static Transformer<String, String> toModuleName() {
		return GUtil::toCamelCase;
	}
	//endregion

	//region Linked file
	private static <T extends Task> Action<T> configureLinkedFile(BiConsumer<? super T, ? super PropertyUtils.Property<RegularFile>> action) {
		return task -> action.accept(task, PropertyUtils.wrap(linkedFileProperty(task)));
	}

	private static Function<ObjectLink, Object> asSharedLibraryFile(Provider<String> baseName) {
		return task -> toolChainProperty(task)
			.map(selectToolProvider(targetPlatformProperty(task)))
			.map(it -> fileNamer(it))
			.orElse(targetPlatformProperty(task).map(it -> fileNamer(it)))
			.flatMap(sharedLibraryLinkedFile(task.getDestinationDirectory(), baseName));
	}

	private interface FileNamer {
		String getSharedLibraryName(String libraryPath);
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
		public String getSharedLibraryName(String libraryPath) {
			return toolProvider.getSharedLibraryName(libraryPath);
		}
	}

	private static final class TargetPlatformFileNamer implements FileNamer {
		private final NativePlatform targetPlatform;

		private TargetPlatformFileNamer(NativePlatform targetPlatform) {
			this.targetPlatform = targetPlatform;
		}

		@Override
		public String getSharedLibraryName(String libraryPath) {
			return ((NativePlatformInternal) targetPlatform).getOperatingSystem().getInternalOs().getSharedLibraryName(libraryPath);
		}
	}

	private static Transformer<Provider<RegularFile>, FileNamer> sharedLibraryLinkedFile(Provider<Directory> destinationDirectory, Provider<String> baseName) {
		return toolProvider -> destinationDirectory.flatMap(dir -> dir.file(baseName.map(toolProvider::getSharedLibraryName)));
	}

	private static Transformer<PlatformToolProvider, NativeToolChain> selectToolProvider(Provider<NativePlatform> nativePlatform) {
		return toolChain -> {
			NativeToolChainInternal toolChainInternal = (NativeToolChainInternal) toolChain;
			return toolChainInternal.select((NativePlatformInternal) nativePlatform.get());
		};
	}

	private static RegularFileProperty linkedFileProperty(Task task) {
		if (task instanceof AbstractLinkTask) {
			return ((AbstractLinkTask) task).getLinkedFile();
		} else {
			throw new IllegalArgumentException();
		}
	}

	private static Property<NativePlatform> targetPlatformProperty(Task task) {
		if (task instanceof AbstractLinkTask) {
			return ((AbstractLinkTask) task).getTargetPlatform();
		} else {
			throw new IllegalArgumentException();
		}
	}

	private static Property<NativeToolChain> toolChainProperty(Task task) {
		if (task instanceof AbstractLinkTask) {
			return ((AbstractLinkTask) task).getToolChain();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion
}
