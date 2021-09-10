package dev.nokee.model

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification

class ConfigurationContainerBridgingFunctionalTest extends AbstractGradleSpecification implements ModelBasePluginSpec {
	def "registers Configuration domain object from other plugins in the model"() {
		buildFile << applyModelBasePlugin() << '''
			apply plugin: 'java'

			assert nokee.modelRegistry.root.get('implementation').canBeViewedAs(Configuration)
			assert nokee.modelRegistry.root.get('compileOnly').canBeViewedAs(Configuration)
			assert nokee.modelRegistry.root.get('runtimeClasspath').canBeViewedAs(Configuration)
		'''

		expect:
		succeeds()
	}

	def "adds known Configuration domain object in the model"() {
		buildFile << applyModelBasePlugin() << '''
			configurations.register('foo')
			configurations.create('bar')

			assert nokee.modelRegistry.root.get('foo').canBeViewedAs(Configuration)
			assert nokee.modelRegistry.root.get('bar').canBeViewedAs(Configuration)
		'''

		expect:
		succeeds()
	}

	def "can register Configuration domain object as projection on existing model node"() {
		buildFile << applyModelBasePlugin() << '''
			nokee.modelRegistry.root.newChildNode('foo')
			nokee.modelRegistry.root.newChildNode('bar')

			configurations.register('foo')
			configurations.create('bar')

			assert nokee.modelRegistry.root.get('foo').canBeViewedAs(Configuration)
			assert nokee.modelRegistry.root.get('bar').canBeViewedAs(Configuration)
		'''

		expect:
		succeeds()
	}

	def "does not realize registered Configuration domain object when bridging into the model"() {
		buildFile << applyModelBasePlugin() << '''
			configurations.register('foo') { throw new UnsupportedOperationException() }

			assert nokee.modelRegistry.root.get('foo').canBeViewedAs(Configuration)
		'''

		expect:
		succeeds()
	}

	def "can create Configuration projection in the model"() {
		buildFile << applyModelBasePlugin() << '''
			nokee.modelRegistry.root.newChildNode('foo').newProjection { it.type(Configuration) }

			assert configurations.findByName('foo') != null
		'''

		expect:
		succeeds()
	}

	def "derives Configuration domain object name from owners when creating projection"() {
		buildFile << applyModelBasePlugin() << '''
			nokee.modelRegistry.root.newChildNode('test').newChildNode('c').newChildNode('headerSearchPaths').newProjection { it.type(Configuration) }

			assert configurations.findByName('testCHeaderSearchPaths') != null
		'''

		expect:
		succeeds()
	}

	def "does not register nested configuration created via the model as owned by the project"() {
		buildFile << applyModelBasePlugin() << '''
			nokee.modelRegistry.root.newChildNode('c').newChildNode('headerSearchPaths').newProjection { it.type(Configuration) }
			assert !nokee.modelRegistry.root.find('cHeaderSearchPaths').present
		'''

		expect:
		succeeds('help')
	}
}
