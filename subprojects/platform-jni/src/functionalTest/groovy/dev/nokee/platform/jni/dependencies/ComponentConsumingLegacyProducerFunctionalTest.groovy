package dev.nokee.platform.jni.dependencies

class ComponentConsumingLegacyProducerFunctionalTest extends AbstractLegacyConfigurationFunctionalTest {
	@Override
	protected void reportDependencies() {
		reportDependencies(DEFAULT)
	}

	@Override
	protected void makeSingleProject() {
		makeSingleProject(DEFAULT)
	}

	protected void assertSharedVariantSelected() {
		assertSharedVariantSelected(DEFAULT, debug)
	}

	protected void assertStaticVariantSelected() {
		assertStaticVariantSelected(DEFAULT, debug)
	}
}
