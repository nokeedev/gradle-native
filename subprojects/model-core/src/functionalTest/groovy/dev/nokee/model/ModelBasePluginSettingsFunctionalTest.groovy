package dev.nokee.model

class ModelBasePluginSettingsFunctionalTest extends AbstractModelBasePluginFunctionalTest {
	def "registers Settings projection on root node"() {
		settingsFile << applyModelBasePlugin() << '''
			assert nokee.modelRegistry.root.canBeViewedAs(Settings)
			assert nokee.modelRegistry.root.get(Settings) == settings
		'''

		expect:
		succeeds()
	}

	@Override
	protected File getBuildscriptFile() {
		return settingsFile
	}
}
