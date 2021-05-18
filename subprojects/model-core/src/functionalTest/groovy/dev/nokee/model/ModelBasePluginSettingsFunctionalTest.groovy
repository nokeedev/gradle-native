package dev.nokee.model

class ModelBasePluginSettingsFunctionalTest extends AbstractModelBasePluginFunctionalTest {
	def "registers nokee extension"() {
		settingsFile << applyModelBasePlugin() << '''
			assert nokee instanceof NokeeExtension
			assert nokee.modelRegistry instanceof ModelRegistry
		'''

		expect:
		succeeds()
	}

	def "registers Settings projection on root node"() {
		settingsFile << applyModelBasePlugin() << '''
			assert nokee.modelRegistry.root.canBeViewedAs(Settings)
			assert nokee.modelRegistry.root.get(Settings) == settings
		'''

		expect:
		succeeds()
	}
}
