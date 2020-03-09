package dev.nokee.language.java

import dev.nokee.language.LanguageTaskNames

trait JavaTaskNames implements LanguageTaskNames {
	/**
	 * Returns the tasks for the root project.
	 */
	ProjectTasks getTasks() {
		return new ProjectTasks('')
	}

	static class ProjectTasks {
		private final String project

		ProjectTasks(String project) {
			this.project = project
		}

		String getCompile() {
			return main.compile
		}

		String getJar() {
			return main.jar
		}

		String getAssemble() {
			return main.assemble
		}

		String getProcessResources() {
			return main.processResources
		}

		List<String> getAllToJar() {
			return main.allToJar
		}

		List<String> getAllToTest() {
			return test.allToClasses + main.allToClasses + [test.test]
		}

		List<String> getAllToAssemble() {
			return allToJar + [assemble]
		}

		MainTasks getMain() {
			return new MainTasks()
		}

		TestTasks getTest() {
			return new TestTasks()
		}

		abstract class VariantTasks {
			protected abstract String getComponentVariantName()

			String getCompile() {
				return withProject("compile${componentVariantName}Java")
			}

			String getProcessResources() {
				return withProject("process${componentVariantName}Resources")
			}

			protected abstract String getClasses()

			List<String> getAllToJar() {
				return allToClasses + [jar]
			}

			List<String> getAllToAssemble() {
				return allToJar + [assemble]
			}

			List<String> getAllToClasses() {
				return [compile, processResources, classes]
			}
		}

		class MainTasks extends VariantTasks {
			@Override
			protected String getComponentVariantName() {
				return ""
			}

			@Override
			protected String getClasses() {
				return withProject('classes')
			}

			String getJar() {
				return withProject("jar")
			}

			String getAssemble() {
				return withProject("assemble")
			}
		}

		class TestTasks extends VariantTasks {
			@Override
			protected String getComponentVariantName() {
				return "Test"
			}

			@Override
			protected String getClasses() {
				return withProject('testClasses')
			}

			String getTest() {
				return withProject('test')
			}
		}

		private withProject(String t) {
			project + ":" + t
		}
	}
}
