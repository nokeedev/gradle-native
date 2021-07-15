package dev.nokee.model

class ModelBasePluginProjectFunctionalTest extends AbstractModelBasePluginFunctionalTest {
	def "registers Project projection on root node"() {
		buildFile << applyModelBasePlugin() << '''
			assert nokee.modelRegistry.root.canBeViewedAs(Project)
			assert nokee.modelRegistry.root.get(Project) == project
		'''

		expect:
		succeeds()
	}

	@Override
	protected String getScriptDslDelegate() {
		return 'project'
	}

	@Override
	protected File getBuildScriptFile() {
		return buildFile
	}
}
