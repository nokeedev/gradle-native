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
package dev.gradleplugins.exemplarkit

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static dev.gradleplugins.exemplarkit.Exemplar.builder
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows

class ExemplarBuilderTest {

	@Nested
	class Empty {
		static Exemplar subject = builder().build()

		@Test
		void "has no steps"() {
			assertThat(subject.steps, emptyIterable())
		}

		@Test
		void "has empty sample"() {
			assertThat(subject.sample, equalTo(Sample.empty()))
		}
	}

	@Nested
	class AddingSteps {
		private final Step newStep() {
			return Step.builder().execute('cat').build()
		}

		@Test
		void "single step"() {
			def step = newStep()
			def exemplar = builder().step(step).build()
			assertThat(exemplar.steps, contains(step))
		}

		@Test
		void "multi-step"() {
			def step1 = newStep()
			def step2 = newStep()
			def exemplar = builder().step(step1).step(step2).build()
			assertThat(exemplar.steps, contains(step1, step2))
		}

		@Test
		void "can use step builder"() {
			def exemplar = builder().step(Step.builder().execute('ls')).build()
			assertThat(exemplar.steps, iterableWithSize(1))
		}

		@Test
		void "throws exception for null step"() {
			def ex = assertThrows(NullPointerException, { builder().step((Step)null) })
			assertEquals("Step cannot be null.", ex.message)
		}

		@Test
		void "throws exception for null step builder"() {
			def ex = assertThrows(NullPointerException, { builder().step((Step.Builder)null) })
			assertEquals("Step builder cannot be null.", ex.message)
		}
	}

	@Test
	void "can source sample from directory"(@TempDir File testDirectory) {
		def sampleDirectory = testDirectory
		def exemplar = builder().fromDirectory(sampleDirectory).build()
		assertEquals(Sample.fromDirectory(sampleDirectory), exemplar.sample)
	}

	@Test
	void "throws exception if sample directory does not exists"(@TempDir File testDirectory) {
		def sampleDirectory = new File(testDirectory, 'non-existant')
		def ex = assertThrows(IllegalArgumentException, { builder().fromDirectory(sampleDirectory) })
		assertEquals("Please specify a valid directory because directory '${testDirectory.absolutePath}${File.separator}non-existant' does not exists.".toString(), ex.message)
	}

	@Test
	void "can source sample from archive"(@TempDir File testDirectory) {
		def sampleArchive = new File(testDirectory, 'foo.zip')
		sampleArchive.createNewFile()
		def exemplar = builder().fromArchive(sampleArchive).build()
		assertEquals(Sample.fromArchive(sampleArchive), exemplar.sample)
	}

	@Test
	void "throws exception if sample archive does not exists"(@TempDir File testDirectory) {
		def sampleArchive = new File(testDirectory, 'foo.zip')
		def ex = assertThrows(IllegalArgumentException, { builder().fromArchive(sampleArchive) })
		assertEquals("Please specify a valid archive because archive '${testDirectory.absolutePath}${File.separator}foo.zip' does not exists.".toString(), ex.message)
	}
}
