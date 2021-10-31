/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.language.base.tasks;

import dev.nokee.language.base.HasDestinationDirectory;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.gradle.platform.base.ToolChain;

public interface SourceCompile extends Task, HasDestinationDirectory {
	/**
	 * The tool chain used for the compilation.
	 *
	 * @return a provider of a {@link ToolChain} instance of the tool chain used for the compilation, never null.
	 */
	@Internal
	Provider<? extends ToolChain> getToolChain();

	/**
	 * <em>Additional</em> arguments to provide to the compiler.
	 *
	 * It act as an escape hatch to the current model.
	 * Please open an issue on https://github.com/nokeedev/gradle-native with your reason for using this hatch so we can improve the model.
	 *
	 * @return a property for adding additional arguments, never null.
	 */
	@Input
	ListProperty<String> getCompilerArgs();

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Internal
	DirectoryProperty getDestinationDirectory();

	/**
	 * Returns the source files to compile.
	 *
	 * @return a file collection containing all the files to compile, never null
	 * @since 0.5
	 */
	@InputFiles
	@SkipWhenEmpty
	@PathSensitive(PathSensitivity.RELATIVE)
	ConfigurableFileCollection getSource();
}
