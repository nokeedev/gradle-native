package dev.nokee.model

class ModelBasePluginProjectFunctionalTest extends AbstractModelBasePluginFunctionalTest {
	def "registers nokee extension"() {
		buildFile << applyModelBasePlugin() << '''
			assert nokee instanceof NokeeExtension
			assert nokee.modelRegistry instanceof ModelRegistry
		'''

		expect:
		succeeds()
	}

	def "registers Project projection on root node"() {
		buildFile << applyModelBasePlugin() << '''
			assert nokee.modelRegistry.root.canBeViewedAs(Project)
			assert nokee.modelRegistry.root.get(Project) == project
		'''

		expect:
		succeeds()
	}
}
