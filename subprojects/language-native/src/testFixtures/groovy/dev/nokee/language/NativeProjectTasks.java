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
package dev.nokee.language;

import java.util.List;

public interface NativeProjectTasks {
	String getBinary();

	List<String> getAllToBinary();

	String getCompile();

	String getObjects();

	String getLink();

	String getCreate();

	String getInstall();

	String getAssemble();

	String getSyncApiElements();

	List<String> getAllToObjects();

	List<String> getAllToLifecycleObjects();

	List<String> getAllToCreate();

	List<String> getAllToLink();

	List<String> getAllToLinkElements();

	List<String> getAllToLinkOrCreate();

	List<String> getAllToInstall();

	List<String> getAllToLifecycleAssemble();

	List<String> getAllToAssemble();

	List<String> getAllToAssembleWithInstall();

	// TODO: split to another interface for test suites
	List<String> getAllToTest();
	List<String> getAllToCheck();
	String getRelocateMainSymbol();

	NativeProjectTasks withOperatingSystemFamily(String operatingSystemFamily);

	NativeProjectTasks withLinkage(String linkage);

	NativeProjectTasks withProjectPath(String projectPath);

	NativeProjectTasks withComponentName(String componentName);

	NativeProjectTasks withBuildType(String buildType);

	NativeProjectTasks getForStaticLibrary();

	NativeProjectTasks getForSharedLibrary();
}
