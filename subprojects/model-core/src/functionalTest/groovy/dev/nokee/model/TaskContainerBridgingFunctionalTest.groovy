package dev.nokee.model

class TaskContainerBridgingFunctionalTest extends AbstractModelBasePluginFunctionalTest {
	def "registers Task domain object from other plugins in the model"() {
		buildFile << applyModelBasePlugin() << '''
			apply plugin: 'lifecycle-base'

			assert nokee.modelRegistry.root.get('tasks').canBeViewedAs(Task)
			assert nokee.modelRegistry.root.get('clean').canBeViewedAs(Task)
			assert nokee.modelRegistry.root.get('build').canBeViewedAs(Task)
			assert nokee.modelRegistry.root.get('assemble').canBeViewedAs(Task)
		'''

		expect:
		succeeds()
	}

	def "adds known Task domain object in the model"() {
		buildFile << applyModelBasePlugin() << '''
			tasks.register('foo')
			tasks.create('bar')

			assert nokee.modelRegistry.root.get('foo').canBeViewedAs(Task)
			assert nokee.modelRegistry.root.get('bar').canBeViewedAs(Task)
		'''

		expect:
		succeeds()
	}

	def "carries Task type to the model node and projection"() {
		buildFile << applyModelBasePlugin() << '''
			tasks.register('foo', Exec)
			tasks.create('bar', JavaCompile)

			assert nokee.modelRegistry.root.get('foo').canBeViewedAs(Exec)
			assert nokee.modelRegistry.root.get('bar').canBeViewedAs(JavaCompile)
		'''

		expect:
		succeeds()
	}

	def "can register Task domain object as projection on existing model node"() {
		buildFile << applyModelBasePlugin() << '''
			nokee.modelRegistry.root.newChildNode('foo')
			nokee.modelRegistry.root.newChildNode('bar')

			tasks.register('foo')
			tasks.create('bar')

			assert nokee.modelRegistry.root.get('foo').canBeViewedAs(Task)
			assert nokee.modelRegistry.root.get('bar').canBeViewedAs(Task)
		'''

		expect:
		succeeds()
	}

	def "does not realize registered Task domain object when bridging into the model"() {
		buildFile << applyModelBasePlugin() << '''
			tasks.register('foo') { throw new UnsupportedOperationException() }

			assert nokee.modelRegistry.root.get('foo').canBeViewedAs(Task)
		'''

		expect:
		succeeds()
	}

	def "can create Task projection in the model"() {
		buildFile << applyModelBasePlugin() << '''
			nokee.model.foo(Sync)
		'''

		expect:
		succeeds('foo')
	}

	def "derives Task domain object name from owners when creating projection"() {
		buildFile << applyModelBasePlugin() << '''
			nokee.model.test.c.compile(Sync)
		'''

		expect:
		succeeds('compileTestC')
	}
}
