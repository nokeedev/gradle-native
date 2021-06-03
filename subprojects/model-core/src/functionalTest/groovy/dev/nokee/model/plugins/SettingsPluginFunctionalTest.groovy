package dev.nokee.model.plugins

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.nokee.model.KnownDomainObject
import dev.nokee.model.dsl.ModelNode

class SettingsPluginFunctionalTest extends AbstractGradleSpecification {
	def "applies using model node and known object settings"() {
		settingsFile << createModelPlugin('''
			void apply(ModelNode node, KnownDomainObject<Settings> knownSettings) {
				assert node instanceof ModelNode
				assert knownSettings instanceof KnownDomainObject
				assert knownSettings.type == Settings
			}
		''') << 'pluginManager.apply(TestPlugin)'

		expect:
		succeeds()
	}

	def "cannot extends base plugin apply method"() {
		settingsFile << createModelPlugin('''
			void apply(Settings settings) {
				// not allowed
			}

			void apply(ModelNode node, KnownDomainObject<Settings> knownSettings) {}
		''') << 'pluginManager.apply(TestPlugin)'

		expect:
		fails()
	}

	def "can access object factory"() {
		settingsFile << createModelPlugin('''
			void apply(ModelNode node, KnownDomainObject<Settings> knownSettings) {
				assert objects instanceof ObjectFactory
			}
		''') << 'apply plugin: TestPlugin'

		expect:
		succeeds()
	}

	def "can access provider factory"() {
		buildFile << createModelPlugin('''
			void apply(ModelNode node, KnownDomainObject<Settings> knownSettings) {
				assert providers instanceof ProviderFactory
			}
		''') << 'apply plugin: TestPlugin'

		expect:
		succeeds()
	}

	def "can access plugin manager"() {
		buildFile << createModelPlugin('''
			void apply(ModelNode node, KnownDomainObject<Settings> knownSettings) {
				assert pluginManager instanceof PluginManager
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
			import ${SettingsPlugin.canonicalName}
			import ${KnownDomainObject.canonicalName}
			class TestPlugin extends SettingsPlugin {
				${content}
			}
		"""
	}
}
