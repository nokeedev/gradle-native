package dev.nokee.platform.base.internal.dependencies

import dev.nokee.platform.base.internal.ProjectIdentifier
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.mapDisplayName
import static dev.nokee.utils.ConfigurationUtils.configureDescription

@Subject(DependencyBucketFactoryImpl)
class DependencyBucketFactoryImplTest extends Specification {
	@Unroll
	def "can create dependency bucket"(bucketType, expectedConfigurationType) {
		given:
		def configurationRegistry = Mock(ConfigurationBucketRegistry)
		def subject = new DependencyBucketFactoryImpl(configurationRegistry, Stub(DependencyHandler))
		def identifier = DependencyBucketIdentifier.of(DependencyBucketName.of('foo'), bucketType, ProjectIdentifier.of('root'))

		when:
		subject.create(identifier)

		then:
		1 * configurationRegistry.createIfAbsent('foo', expectedConfigurationType, configureDescription(mapDisplayName(identifier))) >> Stub(Configuration)

		where:
		bucketType 					| expectedConfigurationType
		DeclarableDependencyBucket 	| ConfigurationBucketType.DECLARABLE
		ResolvableDependencyBucket 	| ConfigurationBucketType.RESOLVABLE
		ConsumableDependencyBucket 	| ConfigurationBucketType.CONSUMABLE
	}
}
