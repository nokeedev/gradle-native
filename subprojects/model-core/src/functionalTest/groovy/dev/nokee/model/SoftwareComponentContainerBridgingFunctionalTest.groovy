package dev.nokee.model

class SoftwareComponentContainerBridgingFunctionalTest extends AbstractModelBasePluginFunctionalTest {
	def "registers SoftwareComponent domain object from other plugins in the model"() {
		buildFile << applyModelBasePlugin() << '''
			apply plugin: 'java'

			assert nokee.modelRegistry.root.get('java').canBeViewedAs(SoftwareComponent)
		'''

		expect:
		succeeds()
	}

	def "adds known SoftwareComponent domain object in the model"() {
		buildFile << applyModelBasePlugin() << declareSoftwareComponentFactoryProvider() << '''
			components.add(objects.newInstance(SoftwareComponentFactoryProvider).get().adhoc("foo"))

			assert nokee.modelRegistry.root.get('foo').canBeViewedAs(AdhocComponentWithVariants)
		'''

		expect:
		succeeds()
	}

	def "can register SoftwareComponent domain object as projection on existing model node"() {
		buildFile << applyModelBasePlugin() << declareSoftwareComponentFactoryProvider() << '''
			nokee.modelRegistry.root.newChildNode('foo')

			components.add(objects.newInstance(SoftwareComponentFactoryProvider).get().adhoc("foo"))

			assert nokee.modelRegistry.root.get('foo').canBeViewedAs(AdhocComponentWithVariants)
		'''

		expect:
		succeeds()
	}

	def "can create SoftwareComponent projection in the model"() {
		buildFile << applyModelBasePlugin() << '''
			nokee.model.foo(AdhocComponentWithVariants)

			assert components.findByName('foo') != null
		'''

		expect:
		succeeds()
	}

	def "derives SoftwareComponent domain object name from owners when creating projection"() {
		buildFile << applyModelBasePlugin() << '''
			nokee.model.test.windows(AdhocComponentWithVariants)

			assert components.findByName('testWindows') != null
		'''

		expect:
		succeeds()
	}

	private static String declareSoftwareComponentFactoryProvider() {
		return '''
			import javax.inject.Inject
			class SoftwareComponentFactoryProvider {
				private final SoftwareComponentFactory factory

				@Inject
				SoftwareComponentFactoryProvider(SoftwareComponentFactory factory) {
					this.factory = factory
				}

				SoftwareComponentFactory get() {
					return factory
				}
			}
		'''
	}
}
