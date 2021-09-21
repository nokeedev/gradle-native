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
package dev.nokee.buildadapter.cmake.internal

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.URIish
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(GitBasedGroupIdSupplier)
class GitBasedGroupIdSupplierTest extends Specification {
	@Rule
	private final TemporaryFolder temporaryFolder = new TemporaryFolder()

	protected void makeSingleRepository(String remoteUri) {
		def git = Git.init().setDirectory(temporaryFolder.root).call()
		git.remoteAdd().setName("origin").setUri(new URIish(remoteUri)).call()
	}

	def "can compute group id for http-based git repository"() {
		given:
		makeSingleRepository("https://github.com/google/googletest")

		expect:
		new GitBasedGroupIdSupplier(temporaryFolder.root).get() == 'com.github.google.googletest'
	}
}
