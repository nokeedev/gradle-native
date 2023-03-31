/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.xcode;

import java.util.List;
import java.util.Set;

public final class DefaultXCTarget implements XCTarget {
	private final String name;
	private final XCProjectReference project;
	private final List<XCFileReference> inputFiles;
	private final XCLoader<Set<XCDependency>, XCTargetReference> dependenciesLoader;
	private final XCFileReference outputFile;

	// friends with XCTargetReference
	DefaultXCTarget(String name, XCProjectReference project, List<XCFileReference> inputFiles, XCFileReference outputFile, XCLoader<Set<XCDependency>, XCTargetReference> dependenciesLoader) {
		this.name = name;
		this.project = project;
		this.outputFile = outputFile;
		this.inputFiles = inputFiles;
		this.dependenciesLoader = dependenciesLoader;
	}

	public String getName() {
		return name;
	}

	public Set<XCDependency> getDependencies() {
		return project.ofTarget(name).load(dependenciesLoader);
	}

	// TODO: Switch to only the input file of THIS XCTarget
	public List<XCFileReference> getInputFiles() {
		return inputFiles;
	}

	public XCFileReference getOutputFile() {
		return outputFile;
	}

	public XCProjectReference getProject() {
		return project;
	}

	@Override
	public XCTargetReference asReference() {
		return project.ofTarget(name);
	}

	@Override
	public String toString() {
		return String.format("target '%s' in project '%s'", name, project.getLocation());
	}
}
