package dev.nokee.ide.xcode.internal.tasks

import dev.nokee.fixtures.tasks.WellBehavingTaskProperty
import dev.nokee.fixtures.tasks.WellBehavingTaskPropertyValue
import dev.nokee.fixtures.tasks.WellBehavingTaskTest
import org.gradle.api.Task

abstract class AbstractSyncXcodeIdeProductWellBehavingTaskTest extends WellBehavingTaskTest {
	@Override
	protected Class<? extends Task> getTaskType() {
		return SyncXcodeIdeProduct
	}
}

class SyncXcodeIdeProductAsFileWellBehavingTaskTest extends AbstractSyncXcodeIdeProductWellBehavingTaskTest {
	@Override
	protected List<WellBehavingTaskProperty> getInputTestCases() {
		def result = []

		result << property('productLocation')
			.withInitialValue(WellBehavingTaskPropertyValue.GroovyDslExpression.of('project.layout.projectDirectory.file("product.exe")'))
			.configureAsProperty()
			.outOfDateWhen(changeFile('product.exe'))
			.incremental(deleteFile('product.exe'), { it ->
				it.result.assertTaskNotSkipped(":${taskUnderTestName}")
				it.file('build/product.exe').assertDoesNotExist()
			})
			.build()

		result << property('destinationLocation')
			.withInitialValue(WellBehavingTaskPropertyValue.GroovyDslExpression.of('project.layout.buildDirectory.file("product.exe")'))
			.configureAsProperty()
			.outOfDateWhen(deleteFile('build/product.exe'))
			.build()

		return result
	}

	@Override
	void assertInitialState() {
		file('build/product.exe').assertIsFile()
	}

	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		file('product.exe').createFile()
	}
}

class SyncXcodeIdeProductAsDirectoryWellBehavingTaskTest extends AbstractSyncXcodeIdeProductWellBehavingTaskTest {
	@Override
	protected List<WellBehavingTaskProperty> getInputTestCases() {
		def result = []

		result << property('productLocation')
			.withInitialValue(WellBehavingTaskPropertyValue.GroovyDslExpression.of('project.layout.projectDirectory.dir("product.app")'))
			.configureAsProperty()
			.outOfDateWhen(changeFile('product.app/product'))
			.incremental(deleteFile('product.app/old-file'), { it ->
				it.result.assertTaskNotSkipped(":${taskUnderTestName}")
				it.file('build/product.app').assertHasDescendants('product')
			})
			.incremental(addFile('product.app/new-file'), { it ->
				it.result.assertTaskNotSkipped(":${taskUnderTestName}")
				it.file('build/product.app').assertHasDescendants('product', 'old-file', 'new-file')
			})
			.incremental(deleteDirectory('product.app'), { it ->
				it.result.assertTaskNotSkipped(":${taskUnderTestName}")
				it.file('build/product.app').assertDoesNotExist()
			})
			.build()

		result << property('destinationLocation')
			.withInitialValue(WellBehavingTaskPropertyValue.GroovyDslExpression.of('project.layout.buildDirectory.file("product.app")'))
			.configureAsProperty()
			.outOfDateWhen(deleteDirectory('build/product.app'))
			.outOfDateWhen(deleteFile('build/product.app/old-file'))
			.outOfDateWhen(changeFile('build/product.app/product'))
			.build()

		return result
	}

	@Override
	void assertInitialState() {
		file('build/product.app').assertHasDescendants('product', 'old-file')
	}

	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		file('product.app').createDirectory()
		file('product.app/product').createFile()
		file('product.app/old-file').createFile()
	}
}
