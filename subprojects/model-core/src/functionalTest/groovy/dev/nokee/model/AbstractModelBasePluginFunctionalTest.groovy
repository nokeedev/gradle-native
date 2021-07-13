package dev.nokee.model

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification

abstract class AbstractModelBasePluginFunctionalTest extends AbstractGradleSpecification implements ModelBasePluginSpec {
	def "registers nokee extension"() {
		buildScriptFile << applyModelBasePlugin() << '''
			assert nokee instanceof NokeeExtension
			assert nokee.modelRegistry instanceof ModelRegistry
		'''

		expect:
		succeeds()
	}

	def "can configure Nokee extension using action instance"() {
		buildScriptFile << applyModelBasePlugin() << '''
			nokee.configure(new Action<NokeeExtension>() {
				void execute(NokeeExtension nokee) {
					nokee.modelRegistry.root.newChildNode('test')
				}
			})

			assert nokee.modelRegistry.root.find('test').present
		'''

		expect:
		succeeds()
	}

	def "can configure Nokee extension using closure instance"() {
		buildScriptFile << applyModelBasePlugin() << '''
			nokee.configure {
				modelRegistry.root.newChildNode('test')
			}

			assert nokee.modelRegistry.root.find('test').present
		'''

		expect:
		succeeds()
	}

	def "can configure Nokee extension using action class"() {
		buildScriptFile << applyModelBasePlugin() << '''
			class MyNokeeRule implements Action<NokeeExtension> {
				void execute(NokeeExtension nokee) {
					nokee.modelRegistry.root.newChildNode('test')
				}
			}
			nokee.configure(MyNokeeRule)
			assert nokee.modelRegistry.root.find('test').present
		'''

		expect:
		succeeds()
	}

	def "can access Nokee extension using safe-accessor"() {
		buildScriptFile << applyModelBasePlugin() << """
			assert NokeeExtension.nokee(${scriptDslDelegate}) instanceof NokeeExtension
		"""

		expect:
		succeeds()
	}

	protected abstract String getScriptDslDelegate()
	protected abstract File getBuildScriptFile()
}
