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
