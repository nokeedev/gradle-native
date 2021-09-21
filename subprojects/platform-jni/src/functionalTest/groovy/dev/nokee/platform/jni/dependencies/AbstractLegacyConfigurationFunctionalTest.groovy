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
package dev.nokee.platform.jni.dependencies

abstract class AbstractLegacyConfigurationFunctionalTest extends AbstractConfigurationFunctionalTest {
	@Override
	protected void makeStaticLibraryProducerProject() {
		producerBuildFile << """
			configurations {
				createIfAbsent('api', configure.asBucket())
				createIfAbsent('implementation', configure.asBucket(api))
				createIfAbsent('headerSearchPathElements', configure.asOutgoingHeaderSearchPathFrom(implementation).andThen {
					outgoing.artifact(file('includes')) {
						type = 'directory'
					}
				})

				create('staticDebugLinkLibraryElements', configure.asOutgoingLinkLibrariesFrom(implementation).asDebug().withStaticLinkage().andThen {
					outgoing.artifact(file('debug/${staticLibraryPath}'))
				})
				create('staticDebugRuntimeLibraryElements', configure.asOutgoingRuntimeLibrariesFrom(implementation).asDebug().withStaticLinkage())

				create('staticReleaseLinkLibraryElements', configure.asOutgoingLinkLibrariesFrom(implementation).asRelease().withStaticLinkage().andThen {
					outgoing.artifact(file('release/${staticLibraryPath}'))
				})
				create('staticReleaseRuntimeLibraryElements', configure.asOutgoingRuntimeLibrariesFrom(implementation).asRelease().withStaticLinkage())
			}
		"""
		file("producer/includes").mkdirs()
		file("producer/debug/${staticLibraryPath}").createFile()
		file("producer/release/${staticLibraryPath}").createFile()
	}

	@Override
	protected void makeSharedLibraryProducerProject() {
		producerBuildFile << """
			configurations {
				createIfAbsent('api', configure.asBucket())
				createIfAbsent('implementation', configure.asBucket(api))
				createIfAbsent('headerSearchPathElements', configure.asOutgoingHeaderSearchPathFrom(implementation).andThen {
					outgoing.artifact(file('includes')) {
						type = 'directory'
					}
				})

				create('sharedDebugLinkLibraryElements', configure.asOutgoingLinkLibrariesFrom(implementation).asDebug().withSharedLinkage().andThen {
					outgoing.artifact(file('debug/${importLibraryPath}'))
				})
				create('sharedDebugRuntimeLibraryElements', configure.asOutgoingRuntimeLibrariesFrom(implementation).asDebug().withSharedLinkage().andThen {
					outgoing.artifact(file('debug/${sharedLibraryPath}'))
				})

				create('sharedReleaseLinkLibraryElements', configure.asOutgoingLinkLibrariesFrom(implementation).asRelease().withSharedLinkage().andThen {
					outgoing.artifact(file('release/${importLibraryPath}'))
				})
				create('sharedReleaseRuntimeLibraryElements', configure.asOutgoingRuntimeLibrariesFrom(implementation).asRelease().withSharedLinkage().andThen {
					outgoing.artifact(file('release/${sharedLibraryPath}'))
				})
			}
		"""
		file("producer/includes").mkdirs()
		file("producer/debug/${importLibraryPath}").createFile()
		file("producer/debug/${sharedLibraryPath}").createFile()
		file("producer/release/${importLibraryPath}").createFile()
		file("producer/release/${sharedLibraryPath}").createFile()
	}
}
