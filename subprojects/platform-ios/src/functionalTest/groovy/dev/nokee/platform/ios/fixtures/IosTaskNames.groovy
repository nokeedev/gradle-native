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
