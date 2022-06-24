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
package dev.nokee.language.swift.internal.plugins;

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.language.base.internal.SourceFiles;
import dev.nokee.language.nativebase.internal.NativeCompileTask;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.OutputDirectoryPath;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.language.swift.SwiftVersion;
import org.gradle.util.GUtil;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static dev.nokee.model.internal.actions.ModelAction.configure;
import static dev.nokee.platform.base.internal.util.PropertyUtils.addAll;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;

final class SwiftCompileTaskDefaultConfigurationRule extends ModelActionWithInputs.ModelAction4<ModelProjection, IdentifierComponent, NativeCompileTask, SourceFiles> {
	private final ModelRegistry registry;

	public SwiftCompileTaskDefaultConfigurationRule(ModelRegistry registry) {
		super(ModelComponentReference.ofProjection(SwiftSourceSetSpec.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(NativeCompileTask.class), ModelComponentReference.of(SourceFiles.class));
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, ModelProjection knownSourceSet, IdentifierComponent identifier, NativeCompileTask compileTask, SourceFiles sourceFiles) {
		registry.instantiate(configure(compileTask.get().getId(), SwiftCompileTask.class, configureModuleFile(convention(ofFileSystemLocationInModulesDirectory(identifier.get(), asModuleFileOfModuleName())))));
		registry.instantiate(configure(compileTask.get().getId(), SwiftCompileTask.class, configureModuleName(convention(toModuleName((ElementName) ((HasName) identifier.get()).getName())))));
		registry.instantiate(configure(compileTask.get().getId(), SwiftCompileTask.class, configureSourceCompatibility(set(SwiftVersion.SWIFT5))));
		registry.instantiate(configure(compileTask.get().getId(), SwiftCompileTask.class, configureSources(from(sourceFiles))));
		registry.instantiate(configure(compileTask.get().getId(), SwiftCompileTask.class, configureDebuggable(convention(false))));
		registry.instantiate(configure(compileTask.get().getId(), SwiftCompileTask.class, configureOptimized(convention(false))));
		registry.instantiate(configure(compileTask.get().getId(), SwiftCompileTask.class, configureCompilerArgs(addAll(forMacOsSdkIfAvailable()))));
	}

	//region Compiler arguments
	private static Action<SwiftCompileTask> configureCompilerArgs(BiConsumer<? super SwiftCompileTask, ? super PropertyUtils.CollectionProperty<String>> action) {
		return task -> action.accept(task, wrap(task.getCompilerArgs()));
	}

	private static Function<SwiftCompileTask, Object> forMacOsSdkIfAvailable() {
		return task -> task.getTargetPlatform().map(it -> {
			if (it.getOperatingSystem().isMacOsX() && OperatingSystem.current().isMacOsX()) {
				// TODO: Support DEVELOPER_DIR or request the xcrun tool from backend
				return ImmutableList.of("-sdk", CommandLine.of("xcrun", "--show-sdk-path").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().getAsString().trim());
			} else {
				return ImmutableList.of();
			}
		});
	}
	//endregion

	//region Module file
	private static Action<SwiftCompileTask> configureModuleFile(BiConsumer<? super SwiftCompileTask, ? super PropertyUtils.Property<RegularFile>> action) {
		return task -> action.accept(task, wrap(task.getModuleFile()));
	}

	private static BiFunction<SwiftCompileTask, Provider<Directory>, Object> asModuleFileOfModuleName() {
		return (t, d) -> d.flatMap(toSwiftModuleFile(t.getModuleName()));
	}

	private static Transformer<Provider<RegularFile>, Directory> toSwiftModuleFile(Provider<? extends String> moduleName) {
		return dir -> dir.file(moduleName.map(m -> m + ".swiftmodule"));
	}

	private static Function<SwiftCompileTask, Object> ofFileSystemLocationInModulesDirectory(DomainObjectIdentifier identifier, BiFunction<? super SwiftCompileTask, ? super Provider<Directory>, ? extends Object> mapper) {
		return task -> mapper.apply(task, task.getProject().getLayout().getBuildDirectory().dir("modules/" + OutputDirectoryPath.fromIdentifier(identifier)));
	}
	//endregion

	//region Module name
	private static Action<SwiftCompileTask> configureModuleName(BiConsumer<? super SwiftCompileTask, ? super PropertyUtils.Property<String>> action) {
		return task -> action.accept(task, wrap(task.getModuleName()));
	}

	private static String toModuleName(ElementName name) {
		return GUtil.toCamelCase(name.toString());
	}
	//endregion

	//region Source compatibility
	private static Action<SwiftCompileTask> configureSourceCompatibility(BiConsumer<? super SwiftCompileTask, ? super PropertyUtils.Property<SwiftVersion>> action) {
		return task -> action.accept(task, wrap(task.getSourceCompatibility()));
	}
	//endregion

	//region Task sources
	private static Action<SwiftCompileTask> configureSources(BiConsumer<? super SwiftCompileTask, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> action.accept(task, wrap(task.getSource()));
	}
	//endregion

	//region Debuggable
	private static Action<SwiftCompileTask> configureDebuggable(BiConsumer<? super SwiftCompileTask, ? super PropertyUtils.Property<Boolean>> action) {
		return task -> action.accept(task, wrap(task.getDebuggable()));
	}
	//endregion

	//region Optimized
	private static Action<SwiftCompileTask> configureOptimized(BiConsumer<? super SwiftCompileTask, ? super PropertyUtils.Property<Boolean>> action) {
		return task -> action.accept(task, wrap(task.getOptimized()));
	}
	//endregion
}
