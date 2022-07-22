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
package dev.nokee.platform.nativebase.internal.linking;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MoreCollectors;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.language.base.HasDestinationDirectory;
import dev.nokee.language.nativebase.internal.NativeToolChainSelector;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.internal.OutputDirectoryPath;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.ExecutableBinary;
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
import org.gradle.internal.os.OperatingSystem;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.Swiftc;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.util.GUtil;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static dev.nokee.model.internal.DomainObjectEntities.newEntity;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.util.PropertyUtils.CollectionProperty;
import static dev.nokee.platform.base.internal.util.PropertyUtils.addAll;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.base.internal.util.PropertyUtils.lockProperty;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.utils.TaskUtils.configureDescription;

final class NativeLinkTaskRegistrationRule extends ModelActionWithInputs.ModelAction2<IdentifierComponent, ModelProjection> {
	private final ModelRegistry registry;
	private final NativeToolChainSelector toolChainSelector;

	public NativeLinkTaskRegistrationRule(ModelRegistry registry, NativeToolChainSelector toolChainSelector) {
		super(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.ofProjection(HasLinkTask.class));
		this.registry = registry;
		this.toolChainSelector = toolChainSelector;
	}

	@Override
	protected void execute(ModelNode entity, IdentifierComponent identifier, ModelProjection projection) {
		@SuppressWarnings("unchecked")
		val implementationType = taskType((ModelType<? extends HasLinkTask<? extends ObjectLink, ? extends ObjectLink>>) projection.getType());

		val linkTask = registry.register(newEntity("link", implementationType, it -> it.ownedBy(entity)));
		linkTask.configure(implementationType, configureDescription("Links the %s.", identifier.get()));
		linkTask.configure(implementationType, configureLinkerArgs(addAll(forMacOsSdkIfAvailable())));
		linkTask.configure(implementationType, configureToolChain(convention(selectToolChainUsing(toolChainSelector)).andThen(lockProperty())));
		if (ModelNodeUtils.canBeViewedAs(entity, of(ExecutableBinary.class))) {
			linkTask.configure(implementationType, configureDestinationDirectory(convention(forExecutable(identifier.get()))));
		} else if (ModelNodeUtils.canBeViewedAs(entity, of(BundleBinary.class))) {
			linkTask.configure(implementationType, configureDestinationDirectory(convention(forBundle(identifier.get()))));
		} else {
			linkTask.configure(implementationType, configureDestinationDirectory(convention(forLibrary(identifier.get()))));
		}
		entity.addComponent(new NativeLinkTask(ModelNodes.of(linkTask)));
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends ObjectLink> taskType(ModelType<? extends HasLinkTask<? extends ObjectLink, ? extends ObjectLink>> type) {
		val t = type.getInterfaces().stream().filter(it -> it.getRawType().equals(HasLinkTask.class)).map(it -> (ModelType<Task>) it).collect(MoreCollectors.onlyElement());
		return (Class<? extends ObjectLink>) ((ParameterizedType) t.getType()).getActualTypeArguments()[1];
	}

	//region Destination directory
	private static <SELF extends Task & HasDestinationDirectory> Action<SELF> configureDestinationDirectory(BiConsumer<? super SELF, ? super PropertyUtils.Property<? extends Directory>> action) {
		return task -> action.accept(task, wrap(task.getDestinationDirectory()));
	}

	private static Function<Task, Provider<Directory>> forLibrary(DomainObjectIdentifier identifier) {
		return task -> task.getProject().getLayout().getBuildDirectory().dir("libs/" + OutputDirectoryPath.fromIdentifier(identifier));
	}

	private static Function<Task, Provider<Directory>> forExecutable(DomainObjectIdentifier identifier) {
		return task -> task.getProject().getLayout().getBuildDirectory().dir("exes/" + OutputDirectoryPath.fromIdentifier(identifier));
	}

	private static Function<Task, Provider<Directory>> forBundle(DomainObjectIdentifier identifier) {
		return task -> task.getProject().getLayout().getBuildDirectory().dir("libs/" + OutputDirectoryPath.fromIdentifier(identifier));
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
		if (task instanceof AbstractLinkTask) {
			return ((AbstractLinkTask) task).getToolChain();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion

	//region Linker arguments
	private static Action<ObjectLink> configureLinkerArgs(BiConsumer<? super ObjectLink, ? super CollectionProperty<String>> action) {
		return task -> action.accept(task, wrap(task.getLinkerArgs()));
	}

	private static Function<ObjectLink, Object> forMacOsSdkIfAvailable() {
		return task -> ((AbstractLinkTask) task).getTargetPlatform().map(it -> {
			if (((AbstractLinkTask) task).getToolChain().isPresent()) {
				if (((AbstractLinkTask) task).getToolChain().get() instanceof Swiftc && it.getOperatingSystem().isMacOsX() && OperatingSystem.current().isMacOsX()) {
					// TODO: Support DEVELOPER_DIR or request the xcrun tool from backend
					return ImmutableList.of("-sdk", CommandLine.of("xcrun", "--show-sdk-path").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().getAsString().trim());
				} else {
					return ImmutableList.of();
				}
			} else {
				return ImmutableList.of();
			}
		}).orElse(ImmutableList.of());
	}

	private static Function<ObjectLink, Object> forSwiftModuleName(DomainObjectProvider<String> baseName) {
		return task -> ((AbstractLinkTask) task).getToolChain().map(it -> {
			if (it instanceof Swiftc) {
				return ImmutableList.of("-module-name", baseName.map(toModuleName()).get());
			} else {
				return ImmutableList.of();
			}
		});
	}

	private static Transformer<String, String> toModuleName() {
		return GUtil::toCamelCase;
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
