package dev.nokee.publish.bintray.internal

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import spock.lang.IgnoreIf
import spock.lang.Subject

@Subject(BintrayPublishPlugin)
class BintrayPublishPluginFunctionalTest extends AbstractGradleSpecification {
	@IgnoreIf({System.getProperties().getProperty("dev.nokee.env.BINTRAY_USER", null) != null})
	def "can publish to bintray"() {
		given:
		buildFile << """
			plugins {
				id 'dev.nokee.experimental.bintray-publish'
			}

			publishing {
				publications {
					foo(MavenPublication) {
						groupId = 'com.examples'
						artifactId = 'foo'
						version = '${nextVersionOf('nokeedev/examples', 'com.examples', 'foo')}'
					}
				}
				repositories {
					maven {
						url = 'https://dl.bintray.com/nokeedev/examples'
						ext.packageName = 'functional-testing'
						credentials {
							username = System.getProperty('dev.nokee.env.BINTRAY_USER')
							password = System.getProperty('dev.nokee.env.BINTRAY_KEY')
						}
					}
				}
			}
		"""

		and:
		executer = executer.withArguments(bintrayCredentials)

		when:
		succeeds('publish')

		then:
		result.assertTasksExecuted(':generatePomFileForFooPublication', ':publish', ':publishFooPublicationToMavenRepository')
	}

	def "fails publishing when missing Bintray package name"() {
		buildFile << '''
			plugins {
				id 'dev.nokee.experimental.bintray-publish'
			}

			publishing {
				publications {
					foo(MavenPublication) {
						groupId = 'com.examples'
						artifactId = 'foo'
						version = '4.2'
					}
				}
				repositories {
					maven {
						url = 'https://dl.bintray.com/nokeedev/examples'
						credentials {
							username = 'a-bintray-user'
							password = 'a-bintray-key'
						}
					}
				}
			}
		'''

		when:
		fails('publish')

		then:
		failure.assertHasCause("When publishing to Bintray repositories, please specify the package name using an extra property on the repository, e.g. ext.packageName = 'foo'.")
	}

	def "fails publishing when missing Bintray credentials are missing"() {
		buildFile << '''
			plugins {
				id 'dev.nokee.experimental.bintray-publish'
			}

			publishing {
				publications {
					foo(MavenPublication) {
						groupId = 'com.examples'
						artifactId = 'foo'
						version = '4.2'
					}
				}
				repositories {
					maven {
						url = 'https://dl.bintray.com/nokeedev/examples'
						ext.packageName = 'foo'
					}
				}
			}
		'''

		when:
		fails('publish')

		then:
		failure.assertHasCause("When publishing to Bintray repositories, please specify the credentials using the credentials DSL on the repository, e.g. credentials { }.")
	}

	def "clean stale output"() {
		buildFile << '''
			plugins {
				id 'dev.nokee.experimental.bintray-publish'
			}

			publishing {
				publications {
					foo(MavenPublication) {
						groupId = 'com.examples'
						artifactId = 'foo'
						version = '4.2'
					}
				}
				repositories {
					maven {
						url = 'https://dl.bintray.com/nokeedev/examples'
						ext.packageName = 'foo'
						credentials {
							username = 'a-bintray-user'
							password = 'a-bintray-key'
						}
					}
				}
			}
		'''

		when:
		succeeds('publish', '-i')
		then:
		result.assertOutputContains('Publishing com/examples/foo/4.2/foo-4.2.pom... done')
		result.assertNotOutput('Publishing com/examples/foo/4.3/foo-4.3.pom... done')

		when:
		buildFile << '''
			publishing.publications.foo.version = '4.3'
		'''
		succeeds('publish', '-i')
		then:
		result.assertNotOutput('Publishing com/examples/foo/4.2/foo-4.2.pom... done')
		result.assertOutputContains('Publishing com/examples/foo/4.3/foo-4.3.pom... done')
	}

	def "can publish to file repositories when using bintray-publish plugin"() {
		buildFile << '''
			plugins {
				id 'dev.nokee.experimental.bintray-publish'
			}

			publishing {
				publications {
					foo(MavenPublication) {
						groupId = 'com.examples'
						artifactId = 'foo'
						version = '4.2'
					}
				}
				repositories {
					maven {
						url = 'https://dl.bintray.com/nokeedev/examples'
						ext.packageName = 'foo'
						credentials {
							username = 'a-bintray-user'
							password = 'a-bintray-key'
						}
					}
					maven {
						name = 'Staging'
						url = layout.buildDirectory.dir('repositories/staging')
					}
				}
			}
		'''

		expect:
		succeeds('publishAllPublicationsToStagingRepository', '-i')
	}

	protected static String[] getBintrayCredentials() {
		return ["-Ddev.nokee.env.BINTRAY_USER=${System.getProperty('dev.nokee.env.BINTRAY_USER')}", "-Ddev.nokee.env.BINTRAY_KEY=${System.getProperty('dev.nokee.env.BINTRAY_KEY')}"]
	}

	private static String nextVersionOf(String repositorySlug, String groupId, String artifactId) {
		return new DefaultVersionNumberFactory().autoIncrementVersionFromMavenArtifactPublishedToBintray(repositorySlug, groupId, artifactId)
	}
}
