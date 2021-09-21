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
package dev.nokee.platform.ios.fixtures

import dev.nokee.language.DefaultNativeProjectTasks
import dev.nokee.language.NativeProjectTaskNames
import dev.nokee.language.NativeProjectTasks

trait IosTaskNames implements NativeProjectTaskNames {
	ProjectTasks forIos(DefaultNativeProjectTasks tasks) {
		return new ProjectTasks(tasks)
	}

	NativeProjectTasks getTasks() {
		return forIos((DefaultNativeProjectTasks)super.getTasks())
	}

	static class ProjectTasks implements NativeProjectTasks {
		@Delegate private final DefaultNativeProjectTasks delegate

		ProjectTasks(DefaultNativeProjectTasks delegate) {
			this.delegate = delegate
		}

		@Override
		String getBinary() {
			return delegate.withProject('bundle')
		}

		@Override
		List<String> getAllToBinary() {
			return null
		}

		@Override
		List<String> getAllToAssemble() {
			return delegate.allToAssemble + [withProject('compileAssetCatalog'), withProject('createApplicationBundle'), withProject('compileStoryboard'), withProject('linkStoryboard'), withProject('processPropertyList'), withProject('signApplicationBundle')]
		}

		@Override
		List<String> getAllToLifecycleAssemble() {
			return delegate.allToAssemble + [withProject('compileAssetCatalog'), withProject('createApplicationBundle'), withProject('compileStoryboard'), withProject('linkStoryboard'), withProject('processPropertyList'), withProject('signApplicationBundle')]
		}
	}
}
