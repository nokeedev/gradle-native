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
