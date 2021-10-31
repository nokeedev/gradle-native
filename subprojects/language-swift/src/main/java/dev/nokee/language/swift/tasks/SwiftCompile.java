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
package dev.nokee.language.swift.tasks;

import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.HasObjectFiles;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

/**
 * Compiles Swift source files into object files.
 *
 * @since 0.4
 */
public interface SwiftCompile extends SourceCompile, HasObjectFiles {
	/**
	 * {@inheritDoc}
	 */
	@OutputFiles
	ConfigurableFileCollection getObjectFiles();

	/**
	 * The modules required to compile the source.
	 *
	 * @return a file collection containing modules required to compile the source, never null
	 * @since 0.5
	 */
	@InputFiles
	@PathSensitive(PathSensitivity.NAME_ONLY)
	ConfigurableFileCollection getModules();

	/**
	 * The location to write the Swift module file to.
	 *
	 * @return a property to configure the location of the Swift module file to produce, never null
	 * @since 0.5
	 */
	@OutputFile
	RegularFileProperty getModuleFile();

	/**
	 * The name of the module to produce.
	 *
	 * @return a property to configure the name of the module to produce, never null
	 * @since 0.5
	 */
	@Optional
	@Input
	Property<String> getModuleName();
}
