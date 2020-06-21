package dev.nokee.platform.jni.fixtures

import dev.nokee.language.NativeLanguageTaskNames
import dev.nokee.language.NativeProjectTaskNames
import dev.nokee.language.NativeProjectTasks

trait JavaJniTaskNames implements NativeProjectTaskNames {
	static ProjectTasks forJni(NativeLanguageTaskNames.ProjectTasks tasks) {
		return new ProjectTasks(tasks)
	}

	NativeProjectTasks getTasks() {
		return forJni((NativeLanguageTaskNames.ProjectTasks)super.getTasks())
	}

	static class ProjectTasks implements NativeProjectTasks {
		@Delegate private final NativeLanguageTaskNames.ProjectTasks delegate

		ProjectTasks(NativeLanguageTaskNames.ProjectTasks delegate) {
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
		List<String> getAllToAssemble() {
			def result = delegate.allToAssemble + [withProject('jar'), withProject('processResources'), withProject('classes'), withProject('compileJava')]
			if (!withVariant('jar').equals('jar')) {
				result += [withProject(withVariant('jar'))]
			}
			return result
		}

		@Override
		NativeProjectTasks withOperatingSystemFamily(String operatingSystemFamily) {
			return new ProjectTasks((NativeLanguageTaskNames.ProjectTasks)delegate.withOperatingSystemFamily(operatingSystemFamily))
		}
	}
}
