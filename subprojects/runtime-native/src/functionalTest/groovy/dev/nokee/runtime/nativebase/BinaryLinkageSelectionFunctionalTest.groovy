package dev.nokee.runtime.nativebase

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification

class BinaryLinkageSelectionFunctionalTest extends AbstractGradleSpecification {

	def setup() {
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << """
			import ${BinaryLinkage.canonicalName}
			configurations {
				sharedElements {
					canBeConsumed = true
					canBeResolved = false
					attributes {
						attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE, objects.named(BinaryLinkage, BinaryLinkage.SHARED))
					}
					outgoing.artifact(file('shared'))
				}
				staticElements {
					canBeConsumed = true
					canBeResolved = false
					attributes {
						attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE, objects.named(BinaryLinkage, BinaryLinkage.STATIC))
					}
					outgoing.artifact(file('static'))
				}
			}
		"""
		buildFile << """
			plugins {
				id 'dev.nokee.runtime-native'
			}

			import ${BinaryLinkage.canonicalName}

			configurations.create('test') {
				canBeConsumed = false
				canBeResolved = true
			}

			def verifyTask = tasks.register('verify')
		"""
	}

	def "can resolve explicit static linkage"() {
		buildFile << '''
			dependencies {
				test(project(':lib')) {
					attributes {
						attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE, objects.named(BinaryLinkage, BinaryLinkage.STATIC))
					}
				}
			}

			verifyTask.configure {
				doLast {
					assert configurations.test.singleFile.name == 'static'
				}
			}
		'''

		expect:
		succeeds('verify')
	}

	def "can resolve explicit shared linkage"() {
		buildFile << '''
			dependencies {
				test(project(':lib')) {
					attributes {
						attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE, objects.named(BinaryLinkage, BinaryLinkage.SHARED))
					}
				}
			}

			verifyTask.configure {
				doLast {
					assert configurations.test.singleFile.name == 'shared'
				}
			}
		'''

		expect:
		succeeds('verify')
	}

	def "defaults to shared linkage when none is provided"() {
		buildFile << '''
			dependencies {
				test(project(':lib'))
			}

			verifyTask.configure {
				doLast {
					assert configurations.test.singleFile.name == 'shared'
				}
			}
		'''

		expect:
		succeeds('verify')
	}
}
