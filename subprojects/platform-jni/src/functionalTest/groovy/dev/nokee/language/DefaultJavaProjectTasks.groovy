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

class DefaultJavaProjectTasks {
	private final String project

	DefaultJavaProjectTasks(String project) {
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
