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

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.SourceFiles;
import dev.nokee.language.nativebase.internal.NativeCompileTask;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.language.swift.SwiftVersion;
import org.gradle.util.GUtil;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toDirectoryPath;
import static dev.nokee.platform.base.internal.util.PropertyUtils.*;

final class SwiftCompileTaskDefaultConfigurationRule extends ModelActionWithInputs.ModelAction3<LanguageSourceSetIdentifier, NativeCompileTask, SourceFiles> {
	private final LanguageSourceSetIdentifier identifier;

	public SwiftCompileTaskDefaultConfigurationRule(LanguageSourceSetIdentifier identifier) {
		this.identifier = identifier;
	}

	@Override
	protected void execute(ModelNode entity, LanguageSourceSetIdentifier identifier, NativeCompileTask compileTask, SourceFiles sourceFiles) {
		if (identifier.equals(this.identifier)) {
			compileTask.configure(SwiftCompileTask.class, configureModuleFile(convention(ofFileSystemLocationInModulesDirectory(identifier, asModuleFileOfModuleName()))));
			compileTask.configure(SwiftCompileTask.class, configureModuleName(convention(toModuleName(identifier.getName()))));
			compileTask.configure(SwiftCompileTask.class, configureSourceCompatibility(set(SwiftVersion.SWIFT5)));
			compileTask.configure(SwiftCompileTask.class, configureSources(from(sourceFiles)));
		}
	}

	//region Module file
	public static Action<SwiftCompileTask> configureModuleFile(BiConsumer<? super SwiftCompileTask, ? super PropertyUtils.Property<RegularFile>> action) {
		return task -> action.accept(task, wrap(task.getModuleFile()));
	}

	public static BiFunction<SwiftCompileTask, Provider<Directory>, Object> asModuleFileOfModuleName() {
		return (t, d) -> d.flatMap(toSwiftModuleFile(t.getModuleName()));
	}

	public static Transformer<Provider<RegularFile>, Directory> toSwiftModuleFile(Provider<? extends String> moduleName) {
		return dir -> dir.file(moduleName.map(m -> m + ".swiftmodule"));
	}

	public static Function<SwiftCompileTask, Object> ofFileSystemLocationInModulesDirectory(DomainObjectIdentifier identifier, BiFunction<? super SwiftCompileTask, ? super Provider<Directory>, ? extends Object> mapper) {
		return task -> mapper.apply(task, task.getProject().getLayout().getBuildDirectory().dir("modules/" + toDirectoryPath(identifier)));
	}
	//endregion

	//region Module name
	public static Action<SwiftCompileTask> configureModuleName(BiConsumer<? super SwiftCompileTask, ? super PropertyUtils.Property<String>> action) {
		return task -> action.accept(task, wrap(task.getModuleName()));
	}

	public static String toModuleName(LanguageSourceSetName name) {
		return GUtil.toCamelCase(name.toString());
	}
	//endregion

	//region Source compatibility
	private static Action<SwiftCompileTask> configureSourceCompatibility(BiConsumer<? super SwiftCompileTask, ? super PropertyUtils.Property<SwiftVersion>> action) {
		return task -> action.accept(task, wrap(task.getSourceCompatibility()));
	}
	//endregion

	//region Task sources
	public static Action<SwiftCompileTask> configureSources(BiConsumer<? super SwiftCompileTask, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> action.accept(task, wrap(task.getSource()));
	}
	//endregion
}
