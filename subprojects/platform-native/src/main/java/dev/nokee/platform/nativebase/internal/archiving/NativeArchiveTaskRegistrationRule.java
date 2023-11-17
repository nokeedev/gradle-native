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

import dev.nokee.language.base.HasDestinationDirectory;
import dev.nokee.language.nativebase.internal.NativeToolChainSelector;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.internal.OutputDirectoryPath;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.nativebase.HasCreateTask;
import dev.nokee.platform.nativebase.tasks.ObjectLink;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
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
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.base.internal.util.PropertyUtils.lockProperty;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;

final class NativeArchiveTaskRegistrationRule implements Action<Artifact> {
	private final NativeToolChainSelector toolChainSelector;

	public NativeArchiveTaskRegistrationRule(NativeToolChainSelector toolChainSelector) {
		this.toolChainSelector = toolChainSelector;
	}

	@Override
	public void execute(Artifact target) {
		if (target instanceof HasCreateTask) {
//			linkTask.configure(implementationType, configureLinkerArgs(addAll(forMacOsSdkIfAvailable())));
			((HasCreateTask) target).getCreateTask().configure(configureToolChain(convention(selectToolChainUsing(toolChainSelector)).andThen(lockProperty())));
		}
	}

	//region Destination directory
	public static <SELF extends Task & HasDestinationDirectory> Action<SELF> configureDestinationDirectory(BiConsumer<? super SELF, ? super PropertyUtils.Property<? extends Directory>> action) {
		return task -> action.accept(task, wrap(task.getDestinationDirectory()));
	}

	public static Function<Task, Provider<Directory>> forLibrary(DomainObjectIdentifier identifier) {
		return task -> task.getProject().getLayout().getBuildDirectory().dir("libs/" + OutputDirectoryPath.forBinary((ModelObjectIdentifier) identifier));
	}
	//endregion

	//region Toolchain
	private static Action<Task> configureToolChain(BiConsumer<? super Task, ? super PropertyUtils.Property<? extends NativeToolChain>> action) {
		return task -> action.accept(task, wrap(toolChainProperty(task)));
	}

	private static Function<Task, Object> selectToolChainUsing(NativeToolChainSelector toolChainSelector) {
		Objects.requireNonNull(toolChainSelector);
		return toolChainSelector::select;
	}

	private static Property<NativeToolChain> toolChainProperty(Task task) {
		if (task instanceof CreateStaticLibraryTask) {
			return ((CreateStaticLibraryTask) task).getToolChain();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion

	//region Linked file
	public static <T extends Task> Action<T> configureLinkedFile(BiConsumer<? super T, ? super PropertyUtils.Property<RegularFile>> action) {
		return task -> action.accept(task, PropertyUtils.wrap(linkedFileProperty(task)));
	}

	public static Function<ObjectLink, Object> asSharedLibraryFile(Provider<String> baseNameProvider) {
		return task -> toolChainProperty(task)
			.map(selectToolProvider(targetPlatformProperty(task)))
			.flatMap(sharedLibraryLinkedFile(task.getDestinationDirectory(), baseNameProvider));
	}

	private static Transformer<Provider<RegularFile>, PlatformToolProvider> sharedLibraryLinkedFile(Provider<Directory> destinationDirectory, Provider<String> baseName) {
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

	public static Property<NativePlatform> targetPlatformProperty(Task task) {
		if (task instanceof AbstractLinkTask) {
			return ((AbstractLinkTask) task).getTargetPlatform();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion
}
