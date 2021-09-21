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
package dev.nokee.ide.visualstudio.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(VisualStudioIdeUtils)
class VisualStudioIdeUtilsTest extends Specification {
	@Rule TemporaryFolder temporaryFolder = new TemporaryFolder()

	def "detects solution not opened when .vs folder doesn't exists"() {
		given:
		def solutionFile = temporaryFolder.newFile('foo.sln')

		expect:
		!VisualStudioIdeUtils.isSolutionCurrentlyOpened(solutionFile)
	}

	def "detects solution not opened when .vs folder exists without any file locked"() {
		given:
		def solutionFile = temporaryFolder.newFile('foo.sln')

		and:
		temporaryFolder.newFolder('.vs')
		temporaryFolder.newFile('.vs/a')
		temporaryFolder.newFile('.vs/b')

		expect:
		!VisualStudioIdeUtils.isSolutionCurrentlyOpened(solutionFile)
	}

	def "detects solution is opened when .vs folder exists with locked files"() {
		given:
		def solutionFile = temporaryFolder.newFile('foo.sln')

		and:
		temporaryFolder.newFolder('.vs')
		temporaryFolder.newFile('.vs/a')
		temporaryFolder.newFolder('.vs', 'b')
		def fileToLock = temporaryFolder.newFile('.vs/b/c')

		and: 'lock one file'
		def inStream = new RandomAccessFile(fileToLock, 'rw')
		def fileChannel = inStream.getChannel()
		def lock = fileChannel.lock()

		expect:
		VisualStudioIdeUtils.isSolutionCurrentlyOpened(solutionFile)

		cleanup:
		lock?.release()
		fileChannel?.close()
		inStream?.close()
	}
}
