package dev.nokee.platform.ios.tasks

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.nokee.platform.ios.tasks.fixtures.WellBehavingTaskProperty
import dev.nokee.platform.ios.tasks.fixtures.WellBehavingTaskPropertyBuilder
import dev.nokee.platform.ios.tasks.fixtures.WellBehavingTaskSpec
import dev.nokee.platform.ios.tasks.fixtures.WellBehavingTaskTransform
import org.gradle.api.Task
import org.gradle.api.tasks.*
import spock.lang.Unroll

import javax.inject.Inject
import java.lang.annotation.Annotation
import java.lang.reflect.Modifier

abstract class WellBehavingTaskTest extends AbstractFunctionalSpec implements WellBehavingTaskSpec {
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

	def "ensure all getters are marked with annotations"() {
		given:
		List<Class<? extends Annotation>> allIncrementalGradleAnnotationTypes = [Input, InputDirectory, InputFiles, InputFile, OutputFile, OutputDirectory, OutputDirectories, Classpath]
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
		succeeds('taskUnderTest')
		then: 'expecting task to be out-of-date'
		testCase.assertState(this)

		where:
		testCase << getInputTestCases()*.incrementalChecks*.testCases.flatten()
	}

	@Unroll("can restore outputs from cache when #testCase.description")
	def "can restore outputs from cache"() {
		given:
		makeSingleProject()
		executer = executer.withBuildCacheEnabled()

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
		testCase << getInputTestCases()*.cachingChecks*.testCases.flatten()
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
			public void applyChangesToProject(TestFile projectDir) {
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
