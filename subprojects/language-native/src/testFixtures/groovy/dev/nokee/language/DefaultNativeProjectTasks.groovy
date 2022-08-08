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
package dev.nokee.language

class DefaultNativeProjectTasks implements NativeProjectTasks {
	private final String project
	private final String languageTaskSuffix
	private final String architecture
	private final String operatingSystemFamily
	private final String buildType
	private final String componentName
	private final String binaryType
	private final String linkage

	DefaultNativeProjectTasks(String project, String languageTaskSuffix) {
		this(project, languageTaskSuffix, null, null, '', null, '', '')
	}

	DefaultNativeProjectTasks(String project, String languageTaskSuffix, String architecture, String operatingSystemFamily, String binaryType, String linkage, String buildType, String componentName) {
		this.project = project
		this.operatingSystemFamily = operatingSystemFamily
		this.architecture = architecture
		this.languageTaskSuffix = languageTaskSuffix
		this.binaryType = binaryType
		this.linkage = linkage
		this.buildType = buildType
		this.componentName = componentName
	}

	DefaultNativeProjectTasks withArchitecture(String architecture) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType, linkage, buildType, componentName)
	}

	DefaultNativeProjectTasks withLinkage(String linkage) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType, linkage, buildType, componentName)
	}

	DefaultNativeProjectTasks withProjectPath(String project) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType, linkage, buildType, componentName)
	}

	DefaultNativeProjectTasks withComponentName(String componentName) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType, linkage, buildType, componentName)
	}

	DefaultNativeProjectTasks withBuildType(String buildType) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType, linkage, buildType, componentName)
	}

	// TODO: This is part of the public API but shouldn't really... used when composing
	String withProject(String t) {
		project + ":" + t
	}

	// TODO: This is part of the public API but shouldn't really... used when composing
	String withVariant(String prefix) {
		return "${prefix}${variant.capitalize()}"
	}

	// TODO: This is part of the public API but shouldn't really... used when composing
	String withVariant(String prefix, String suffix) {
		return "${prefix}${variant.capitalize()}${suffix.capitalize()}"
	}

	DefaultNativeProjectTasks withOperatingSystemFamily(String operatingSystemFamily) {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, binaryType, linkage, buildType, componentName)
	}

	// TODO: Return a specialized type for shared library binary
	DefaultNativeProjectTasks getForSharedLibrary() {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, 'sharedLibrary', linkage, buildType, componentName)
	}
	DefaultNativeProjectTasks getForStaticLibrary() {
		return new DefaultNativeProjectTasks(project, languageTaskSuffix, architecture, operatingSystemFamily, 'staticLibrary', linkage, buildType, componentName)
	}
	String getBinary() {
		return withProject(withVariant("${binaryType}${buildType.capitalize()}"))
	}
	List<String> getAllToBinary() {
		return allToLink + [binary]
	}

	String getCompile() {
		return withProject(withVariant("compile", languageTaskSuffix))
	}

	String getObjects() {
		return withProject(withVariant("objects"))
	}

	String getLink() {
		return withProject(withVariant("link"))
	}

	String getCreate() {
		return withProject(withVariant("create"))
	}

	String getLinkOrCreate() {
		if (binaryType == 'staticLibrary') {
			return create
		}
		return link
	}

	String getInstall() {
		return withProject(withVariant("install"))
	}

	String getAssemble() {
		return withProject(withVariant("assemble"))
	}

	@Override
	String getSyncApiElements() {
		if (languageTaskSuffix == 'Swift') {
			throw new UnsupportedOperationException('No supported for Swift yet.')
		}

		return withProject(withVariant('sync')) + 'PublicHeaders'
	}

	List<String> getAllToObjects() {
		return [compile, objects]
	}

	List<String> getAllToLifecycleObjects() {
		return [compile, withProject('objects')]
	}

	List<String> getAllToCreate() {
		return [compile, create]
	}

	List<String> getAllToLink() {
		return [compile, link]
	}

	List<String> getAllToLinkOrCreate() {
		return [compile, linkOrCreate]
	}

	List<String> getAllToLinkElements() {
		return allToLinkOrCreate + (languageTaskSuffix != 'Swift' ? [syncApiElements] : [])
	}

	List<String> getAllToInstall() {
		return allToLink + [install]
	}

	List<String> getAllToLifecycleAssemble() {
		return allToLinkOrCreate + [withProject('assemble')]
	}

	List<String> getAllToAssemble() {
		return allToLinkOrCreate + [assemble]
	}

	List<String> getAllToAssembleWithInstall() {
		return allToInstall + [assemble]
	}

	String getRelocateMainSymbol() {
		return withProject("relocateMainSymbolFor${componentName.capitalize()}${buildType.capitalize()}")
	}

	List<String> getAllToTest() {
		if (binaryType == 'staticLibrary' || binaryType == 'sharedLibrary') {
			return allToLink + [withProject(withVariant('run')), withProject(variant)]
		}
		return allToLink + [relocateMainSymbol, withProject(withVariant('run')), withProject(variant)]
	}

	List<String> getAllToCheck() {
		return allToTest + [withProject('check')]
	}

	protected String getVariant() {
		String result = componentName
		result += buildType.capitalize()
		if (operatingSystemFamily != null) {
			result += operatingSystemFamily.toLowerCase().capitalize()
		}
		if (architecture != null) {
			result += architecture.toLowerCase().capitalize()
		}
		if (linkage != null) {
			result += linkage.toLowerCase().capitalize()
		}
		return result
	}
}
