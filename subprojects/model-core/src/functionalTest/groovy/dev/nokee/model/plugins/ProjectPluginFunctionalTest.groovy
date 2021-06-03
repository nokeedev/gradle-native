package dev.nokee.model.plugins

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.nokee.model.KnownDomainObject
import dev.nokee.model.dsl.ModelNode

class ProjectPluginFunctionalTest extends AbstractGradleSpecification {
	def "applies using model node and known object project"() {
		buildFile << createModelPlugin('''
			void apply(ModelNode node, KnownDomainObject<Project> knownProject) {
				assert node instanceof ModelNode
				assert knownProject instanceof KnownDomainObject
				assert knownProject.type == Project
			}
		''') << 'apply plugin: TestPlugin'

		expect:
		succeeds()
	}

	def "cannot extends base plugin apply method"() {
		buildFile << createModelPlugin('''
			void apply(Project project) {
				// not allowed
			}

			void apply(ModelNode node, KnownDomainObject<Project> knownProject) {}
		''') << 'apply plugin: TestPlugin'

		expect:
		fails()
	}

	def "can access object factory"() {
		buildFile << createModelPlugin('''
			void apply(ModelNode node, KnownDomainObject<Project> knownProject) {
				assert objects instanceof ObjectFactory
			}
		''') << 'apply plugin: TestPlugin'

		expect:
		succeeds()
	}

	def "can access provider factory"() {
		buildFile << createModelPlugin('''
			void apply(ModelNode node, KnownDomainObject<Project> knownProject) {
				assert providers instanceof ProviderFactory
			}
		''') << 'apply plugin: TestPlugin'

		expect:
		succeeds()
	}

	def "can access plugin manager"() {
		buildFile << createModelPlugin('''
			void apply(ModelNode node, KnownDomainObject<Project> knownProject) {
				assert pluginManager instanceof PluginManager
			}
		''') << 'apply plugin: TestPlugin'

		expect:
		succeeds()
	}

	def "can access project layout"() {
		buildFile << createModelPlugin('''
			void apply(ModelNode node, KnownDomainObject<Project> knownProject) {
				assert layout instanceof ProjectLayout
			}
		''') << 'apply plugin: TestPlugin'

		expect:
		succeeds()
	}

	def "can access dependencies"() {
		buildFile << createModelPlugin('''
			void apply(ModelNode node, KnownDomainObject<Project> knownProject) {
				assert dependencies instanceof DependencyHandler
			}
		''') << 'apply plugin: TestPlugin'

		expect:
		succeeds()
	}

	// for bridging into/from software model
	def "can access Gradle model registry"() {
		buildFile << createModelPlugin('''
			void apply(ModelNode node, KnownDomainObject<Project> knownProject) {
				assert modelRegistry instanceof org.gradle.model.internal.registry.ModelRegistry
			}
		''') << 'apply plugin: TestPlugin'

		expect:
		succeeds()
	}

	private static String createModelPlugin(String content) {
		return """
			plugins {
				id 'dev.nokee.model-base'
			}

			import ${ModelNode.canonicalName}
			import ${ProjectPlugin.canonicalName}
			import ${KnownDomainObject.canonicalName}
			class TestPlugin extends ProjectPlugin {
				${content}
			}
		"""
	}
}
