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
package dev.nokee.platform.jni.fixtures

import dev.nokee.language.DefaultNativeProjectTasks
import dev.nokee.language.NativeProjectTaskNames
import dev.nokee.language.NativeProjectTasks

trait JavaJniTaskNames implements NativeProjectTaskNames {
	static ProjectTasks forJni(DefaultNativeProjectTasks tasks) {
		return new ProjectTasks(tasks)
	}

	NativeProjectTasks getTasks() {
		return forJni((DefaultNativeProjectTasks)super.getTasks())
	}

	static class ProjectTasks implements NativeProjectTasks {
		@Delegate private final DefaultNativeProjectTasks delegate

		ProjectTasks(DefaultNativeProjectTasks delegate) {
			this.delegate = delegate
		}

		@Override
		String getBinary() {
			return delegate.withProject('sharedLibrary')
		}

		@Override
		List<String> getAllToBinary() {
			return null
		}

		@Override
		List<String> getAllToObjects() {
			return delegate.allToObjects + [withProject('compileJava')]
		}

		@Override
		List<String> getAllToLifecycleObjects() {
			return delegate.allToLifecycleObjects + [withProject('compileJava')]
		}

		@Override
		List<String> getAllToAssemble() {
			return addAdditionalAssembleTasks(delegate.allToAssemble)
		}

		@Override
		List<String> getAllToLifecycleAssemble() {
			return addAdditionalAssembleTasks(delegate.allToLifecycleAssemble)
		}

		private List<String> addAdditionalAssembleTasks(List<String> allToAssembleTasks) {
			def result = allToAssembleTasks + [withProject('jar'), withProject('processResources'), withProject('classes'), withProject('compileJava')]
			if (!withVariant('jar').equals('jar')) {
				result += [withProject(withVariant('jar'))]
			}
			return result
		}

		@Override
		NativeProjectTasks withOperatingSystemFamily(String operatingSystemFamily) {
			return new ProjectTasks((DefaultNativeProjectTasks)delegate.withOperatingSystemFamily(operatingSystemFamily))
		}

		@Override
		NativeProjectTasks withLinkage(String linkage) {
			return new ProjectTasks((DefaultNativeProjectTasks)delegate.withLinkage(linkage))
		}
	}
}
