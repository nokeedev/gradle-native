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
package dev.nokee.fixtures.tasks

import com.google.common.collect.ImmutableList
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile
import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.test.fixtures.file.TestFile
import org.gradle.api.Task
import org.gradle.api.tasks.*
import org.hamcrest.Matchers
import spock.lang.Unroll

import javax.inject.Inject
import java.lang.annotation.Annotation
import java.lang.reflect.Modifier

import static org.junit.Assume.assumeThat

abstract class WellBehavingTaskTest extends AbstractGradleSpecification implements WellBehavingTaskSpec {
	protected abstract Class<? extends Task> getTaskType();

	//region WellBehavingTaskSpec
	@Override
	TestFile getTestDirectory() {
		return super.getTestDirectory()
	}

	@Override
	TestFile getBuildFile() {
		return super.getBuildFile()
	}
	//endregion

	// TODO: Maybe have similar check as validatePlugins, see https://scans.gradle.com/s/jn2rqe3hv3his/console-log#L327

	def "ensure all getters are marked with annotations"() {
		given:
		// TODO: Recursive check for @Nested
		// TODO: @Internal should probably always have a string to inform why it's internal
		List<Class<? extends Annotation>> allIncrementalGradleAnnotationTypes = [Input, InputDirectory, InputFiles, InputFile, OutputFile, OutputFiles, OutputDirectory, OutputDirectories, Classpath, Nested, Internal]
		Closure nonPrivateMethods = { (it.modifiers & Modifier.PRIVATE) == 0 }
		Closure nonStaticMethods = { (it.modifiers & Modifier.STATIC) == 0 }
		Closure nonInjectAnnotatedMethods = { !it.declaredAnnotations*.annotationType().contains(Inject) }
		Closure getterMethods = { it.name =~ /^(get|is)[A-Z]/ }

		def allGetterMethods = taskType.declaredMethods
			.findAll(getterMethods)
			.findAll(nonPrivateMethods)
			.findAll(nonStaticMethods)
			.findAll(nonInjectAnnotatedMethods)

		expect:
		assert allGetterMethods*.declaredAnnotations.every { !it*.annotationType().empty && it*.annotationType().intersect(allIncrementalGradleAnnotationTypes) }
	}

	@Unroll("can detect changes when #testCase.description")
	def "can detect changes"() {
		given:
		makeSingleProject()

		when:
		succeeds('taskUnderTest')
		then:
		result.assertTasksExecutedAndNotSkipped(':taskUnderTest')

		when:
		succeeds('taskUnderTest')
		then: 'expecting task to be up-to-date'
		result.assertTasksSkipped(':taskUnderTest')

		when:
		testCase.applyChanges(this)
		succeeds('taskUnderTest')
		then: 'expecting task to be out-of-date'
		testCase.assertState(this)

		where:
		testCase << getInputTestCases()*.upToDateChecks*.testCases.flatten()
	}

	@Unroll("can execute incrementally when #testCase.description")
	def "can execute incrementally"() {
		given:
		makeSingleProject()

		when:
		succeeds('taskUnderTest')
		then:
		result.assertTasksExecutedAndNotSkipped(':taskUnderTest')

		when:
		succeeds('taskUnderTest')
		then: 'expecting task to be up-to-date'
		result.assertTasksSkipped(':taskUnderTest')

		when:
		testCase.applyChanges(this)
		succeeds('taskUnderTest', '-i')
		then: 'expecting task to be out-of-date'
		testCase.assertState(this)

		where:
		testCase << getInputTestCases()*.incrementalChecks*.testCases.flatten()
	}

	@Unroll("can restore outputs from cache when #testCase.description")
	def "can restore outputs from cache"(testCase) {
		assumeThat(testCase, Matchers.not(Matchers.isA(WellBehavingTaskTestCase.SkippingTestCase)))

		given:
		makeSingleProject()
		executer = executer.requireOwnGradleUserHomeDirectory().withBuildCacheEnabled()

		when:
		succeeds('taskUnderTest')
		then:
		result.assertTasksExecutedAndNotSkipped(':taskUnderTest')

		when:
		succeeds('taskUnderTest')
		then: 'expecting task to be up-to-date'
		result.assertTasksSkipped(':taskUnderTest')

		when:
		testCase.applyChanges(this)
		succeeds('taskUnderTest')
		then: 'expecting task to be out-of-date'
		testCase.assertState(this)

		where:
		testCase << getAllCachingTestCases()
	}

	protected List<WellBehavingTaskTestCase> getAllCachingTestCases() {
		if (!taskType.declaredAnnotations.contains(CacheableTask)) {
			return ImmutableList.of(WellBehavingTaskTestCase.ignore())
		}
		return getInputTestCases()*.cachingChecks*.testCases.flatten()
	}

	protected void makeSingleProject() {
		buildFile << configurePluginClasspathAsBuildScriptDependencies() << """
			import ${taskType.canonicalName}

			tasks.register('taskUnderTest', ${taskType.simpleName}) {
				${configureInitialValues()}
			}
		"""
	}

	protected final String getTaskUnderTestDsl() {
		return "tasks.named('taskUnderTest', ${taskType.simpleName})"
	}

	protected final String getTaskUnderTestName() {
		return 'taskUnderTest'
	}

	protected String configureInitialValues() {
		return getInputTestCases().collect {
			"""
			 	${it.configure()}
			"""
		}.join('\n')
	}

	protected abstract List<WellBehavingTaskProperty> getInputTestCases()

	protected static WellBehavingTaskTransform deleteDirectory(final String path) {
		return new WellBehavingTaskTransform() {
			@Override
			String getDescription() {
				return "deleting directory '${path}'"
			}

			@Override
			void applyChanges(WellBehavingTaskSpec context) {
				context.file(path).assertIsDirectory()
				assert context.file(path).deleteDir()
			}
		}
	}

	protected static WellBehavingTaskTransform deleteFile(String path) {
		return new WellBehavingTaskTransform() {
			@Override
			String getDescription() {
				return "delete file '${path}'"
			}

			@Override
			void applyChanges(WellBehavingTaskSpec context) {
				context.file(path).assertIsFile()
				assert context.file(path).delete()
			}
		}
	}

	protected static WellBehavingTaskTransform cleanDirectory(String path) {
		return new WellBehavingTaskTransform() {
			@Override
			String getDescription() {
				return "removing all elements inside '${path}'"
			}

			@Override
			void applyChanges(WellBehavingTaskSpec context) {
				context.file(path).assertIsDirectory()

				// TODO: Add to TestFile
//				projectDirectory.file(path).cleanDirectory()
				context.file(path).listFiles().each {
					if (it.isDirectory()) {
						assert it.deleteDir()
					} else {
						assert it.delete()
					}
				}
				context.file(path).assertIsDirectory()
			}
		}
	}

	protected static WellBehavingTaskTransform changeFile(String path) {
		return new WellBehavingTaskTransform() {
			@Override
			String getDescription() {
				return "modifying file '${path}'"
			}

			@Override
			void applyChanges(WellBehavingTaskSpec context) {
				context.file(path).assertIsFile()
				context.file(path) << 'additional data'
			}
		}
	}

	protected static WellBehavingTaskTransform addFile(String path) {
		return new WellBehavingTaskTransform() {
			@Override
			String getDescription() {
				return "add file '${path}'"
			}

			@Override
			void applyChanges(WellBehavingTaskSpec context) {
				context.file(path).assertDoesNotExist()
				context.file(path).text = 'foo'
			}
		}
	}

	protected static FileTransform delete(final SourceElement beforeElement) {
		final String sourceSetName = beforeElement.getSourceSetName();
		final List<SourceFile> beforeFiles = beforeElement.getFiles();

		return new FileTransform() {
			@Override
			void applyChangesToProject(TestFile projectDir) {
				for (SourceFile beforeFile : beforeFiles) {
					TestFile file = projectDir.file(beforeFile.withPath("src/" + sourceSetName));
					file.assertExists();

					file.delete();
				}
			}

			@Override
			public List<SourceFile> getBeforeFiles() {
				return beforeElement.getFiles();
			}

			@Override
			public List<SourceFile> getAfterFiles() {
				return Collections.emptyList();
			}
		};
	}

	static interface FileTransform {
		void applyChangesToProject(TestFile projectDirectory);

		List<SourceFile> getBeforeFiles();

		List<SourceFile> getAfterFiles();
	}

	// Keeping the following code as it has some useful query on the taskType to extract information from the properties
//	protected TaskProperty property(String name) {
//		def method = taskType.declaredMethods.find { it.name == "get${name.capitalize()}" }
//
//		assert method != null, "Property $name doesn't exists on ${taskType}"
//		if (method.genericReturnType instanceof ParameterizedType) {
//			//... extract generic type
//		}
//
//		if (method.returnType == Property) {
//			def propertyType = (Class<?>)((ParameterizedType)method.genericReturnType).actualTypeArguments[0]
//			def canBeNull = method.declaredAnnotations*.annotationType().contains(Optional)
//			return new PropertyTaskProperty(name, method, propertyType, canBeNull)
//		} else if (method.returnType == ConfigurableFileCollection) {
//			return new FileCollectionTaskProperty(name, method, File)
//		} else if (method.returnType == DirectoryProperty) {
//			return new PropertyTaskProperty(name, method, File, false)
//		}
//		throw new UnsupportedOperationException("Unfortunately the property type is not supported at the moment ($name, ${method.returnType})")
//	}

	protected WellBehavingTaskPropertyBuilder property(String propertyName) {
		return new WellBehavingTaskPropertyBuilder(propertyName, taskType)
	}
}
