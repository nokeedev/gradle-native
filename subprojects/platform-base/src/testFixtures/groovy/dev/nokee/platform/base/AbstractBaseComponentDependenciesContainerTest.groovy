package dev.nokee.platform.base

import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.dependencies.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

abstract class AbstractBaseComponentDependenciesContainerTest<T extends BaseComponentDependenciesContainer> extends Specification {
	final ComponentDependenciesContainer delegate = Mock()

	protected abstract T newSubject(ComponentDependenciesContainer delegate)

	def "forwards register(DependencyBucketName, Class) method to delegate"() {
		given:
		def subject = newSubject(delegate)
		def bucketName = DependencyBucketName.of('foo')

		when:
		subject.register(bucketName, DeclarableDependencies)

		then:
		1 * delegate.register(bucketName, DeclarableDependencies)
		0 * _
	}

	def "forwards register(DependencyBucketName, Class, Action) method to delegate"() {
		given:
		def subject = newSubject(delegate)
		def bucketName = DependencyBucketName.of('foo')
		def action = Mock(Action)

		when:
		subject.register(bucketName, DeclarableDependencies, action)

		then:
		1 * delegate.register(bucketName, DeclarableDependencies, action)
		0 * _
	}

	def "forwards configureEach(Action) method to delegate"() {
		given:
		def subject = newSubject(delegate)
		def action = Mock(Action)

		when:
		subject.configureEach(action)

		then:
		1 * delegate.configureEach(action)
		0 * _
	}

	def "forwards findByName(DependencyBucketName) method to delegate"() {
		given:
		def subject = newSubject(delegate)
		def bucketName = DependencyBucketName.of('foo')

		when:
		subject.findByName(bucketName)

		then:
		1 * delegate.findByName(bucketName)
		0 * _
	}

	// =-=-=-=- Integration tests -=-=-=-=

	//region Predefined buckets
	protected Set<String> getPredefinedBucketNames() {
		return getPredefinedDeclarableBucketNames() + getPredefinedResolvableBucketNames() + getPredefinedConsumableBucketNames()
	}

	protected abstract Set<String> getPredefinedDeclarableBucketNames()

	protected Set<String> getPredefinedResolvableBucketNames() {
		return []
	}

	protected Set<String> getPredefinedConsumableBucketNames() {
		return []
	}

	protected abstract DependencyBucketInstantiator newInstantiator(Project project)

	def "creates predefined dependency buckets"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)

		when:
		newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))

		then:
		project.configurations*.name as Set == predefinedBucketNames
	}

	@Unroll
	def "can add dependencies on predefined declarable dependency buckets"(bucketName) {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))
		def notation = 'com.example:foo:4.2'

		when:
		subject."${bucketName}"(notation)

		then:
		def dependency = one(project.configurations."${bucketName}".dependencies)
		dependency.group == 'com.example'
		dependency.name == 'foo'
		dependency.version == '4.2'

		where:
		bucketName << predefinedDeclarableBucketNames
	}

	@Unroll
	def "can add dependencies with action on predefined declarable dependency buckets"(bucketName) {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))
		def notation = 'com.example:foo:4.2'
		def action = Mock(Action)

		when:
		subject."${bucketName}"(notation, action)

		then:
		1 * action.execute(_)

		and:
		def dependency = one(project.configurations."${bucketName}".dependencies)
		dependency.group == 'com.example'
		dependency.name == 'foo'
		dependency.version == '4.2'

		where:
		bucketName << predefinedDeclarableBucketNames
	}

	@Unroll
	def "can get predefined dependency buckets"(bucketName) {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))

		when:
		def bucket = subject."get${bucketName.capitalize()}"()
		then:
		bucket == subject.findByName(DependencyBucketName.of(bucketName)).get()

		where:
		bucketName << predefinedBucketNames
	}

	@Unroll
	def "predefined dependency buckets are of the expected type"(bucketName, bucketType) {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))

		expect:
		bucketType.isAssignableFrom(subject."get${bucketName.capitalize()}"().class)

		where:
		[bucketName, bucketType] << ([predefinedDeclarableBucketNames, [DeclarableDependencyBucket]].combinations() + [predefinedConsumableBucketNames, [ConsumableDependencyBucket]].combinations() + [predefinedResolvableBucketNames, [ResolvableDependencyBucket]].combinations())
	}
	//endregion

	//region Groovy DSL decoration
	def "can access existing bucket as property"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))
		subject.register(DependencyBucketName.of('foo'), DeclarableDependencies)

		expect:
		subject.foo == subject.findByName(DependencyBucketName.of('foo')).get()
	}

	private static <T> T one(Iterable<T> iterable) {
		assert iterable.size() == 1
		return iterable.first()
	}

	def "can add dependency as method call on for dynamically added bucket"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))
		subject.register(DependencyBucketName.of('foo'), DeclarableDependencies)

		when:
		subject.foo('com.example:foo:4.2')

		then:
		def dependency = one(project.configurations.foo.dependencies)
		dependency.group == 'com.example'
		dependency.name == 'foo'
		dependency.version == '4.2'
	}

	def "can include configuration when adding dependency as method call"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))
		subject.register(DependencyBucketName.of('foo'), DeclarableDependencies)

		when:
		def configuredDependencies = []
		subject.foo('com.example:foo:4.2') { ModuleDependency dependency ->
			assert delegate instanceof ModuleDependency
			configuredDependencies << "${dependency.group}:${dependency.name}:${dependency.version}"
		}

		then:
		configuredDependencies == ['com.example:foo:4.2']
	}

	def "throws exceptions when adding dependency as method call with configuration action to missing bucket"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))

		when:
		subject.missing('com.example:foo:4.2') {
			throw new AssertionError()
		}

		then:
		thrown(MissingMethodException)
	}

	def "throws exceptions when adding dependency as method call to missing bucket"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))

		when:
		subject.missing('com.example:foo:4.2')

		then:
		thrown(MissingMethodException)
	}

	def "throws exceptions when accessing missing bucket as property"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))

		when:
		subject.missing

		then:
		def ex = thrown(MissingPropertyException)
		ex.message.startsWith 'No such property: missing for class:'
	}

	def "does not add dynamic dependency registration method for consumable dependency bucket"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))

		and:
		subject.register(DependencyBucketName.of('foo'), ConsumableDependencies)

		when:
		subject.foo('com.example:foo:4.2')
		then:
		thrown(MissingMethodException)

		when:
		subject.foo('com.example:foo:4.2') {}
		then:
		thrown(MissingMethodException)
	}

	def "does not add dynamic dependency registration method for resolvable dependency bucket"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))

		and:
		subject.register(DependencyBucketName.of('foo'), ResolvableDependencies)

		when:
		subject.foo('com.example:foo:4.2')
		then:
		thrown(MissingMethodException)

		when:
		subject.foo('com.example:foo:4.2') {}
		then:
		thrown(MissingMethodException)
	}

	def "can register configureEach configuration groovy closure with delegates to first parameter"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = newInstantiator(project)
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))

		and:
		subject.register(DependencyBucketName.of('foo'), ResolvableDependencies)

		when:
		def capturedDelegate = null
		subject.configureEach {
			if (it.name.get() == 'foo') {
				capturedDelegate = delegate
			}
		}

		then:
		capturedDelegate instanceof ResolvableDependencies
	}
	//endregion
}
