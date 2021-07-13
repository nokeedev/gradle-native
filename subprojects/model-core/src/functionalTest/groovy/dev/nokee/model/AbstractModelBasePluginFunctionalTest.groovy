package dev.nokee.model

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification

abstract class AbstractModelBasePluginFunctionalTest extends AbstractGradleSpecification implements ModelBasePluginSpec {
	def "registers nokee extension"() {
		buildscriptFile << applyModelBasePlugin() << '''
			assert nokee instanceof NokeeExtension
			assert nokee.modelRegistry instanceof ModelRegistry
		'''

		expect:
		succeeds()
	}

	protected abstract File getBuildscriptFile();
}
