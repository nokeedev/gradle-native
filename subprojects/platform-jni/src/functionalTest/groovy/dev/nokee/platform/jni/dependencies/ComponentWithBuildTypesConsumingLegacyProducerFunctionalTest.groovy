package dev.nokee.platform.jni.dependencies

class ComponentWithBuildTypesConsumingLegacyProducerFunctionalTest extends AbstractLegacyConfigurationFunctionalTest {
	@Override
	protected void reportDependencies() {
		reportDependencies(debug, release)
	}

	@Override
	protected void makeSingleProject() {
		makeSingleProject(debug, release)
	}

	protected void assertSharedVariantSelected() {
		assertSharedVariantSelected(debug, debug)
		assertSharedVariantSelected(release, release)
	}

	protected void assertStaticVariantSelected() {
		assertStaticVariantSelected(debug, debug)
		assertStaticVariantSelected(release, release)
	}
}
