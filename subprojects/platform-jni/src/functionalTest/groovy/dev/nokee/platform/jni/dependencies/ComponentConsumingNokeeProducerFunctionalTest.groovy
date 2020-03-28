package dev.nokee.platform.jni.dependencies

class ComponentConsumingNokeeProducerFunctionalTest extends AbstractNokeeConfigurationFunctionalTest {
	@Override
	protected void reportDependencies() {
		reportDependencies(DEFAULT)
	}

	@Override
	protected void makeSingleProject() {
		makeSingleProject(DEFAULT)
	}

	@Override
	protected void assertStaticVariantSelectedMatchingBuildTypes(BuildType... producer) {
		assertStaticVariantSelected(DEFAULT, debug)
	}

	@Override
	protected void assertSharedVariantSelectedMatchingBuildTypes(BuildType... producer) {
		assertSharedVariantSelected(DEFAULT, debug)
	}

	@Override
	protected void assertFrameworkVariantSelectedMatchingBuildTypes(BuildType... producer) {
		assertFrameworkVariantSelected(DEFAULT, debug)
	}

	@Override
	protected void assertSharedVariantSelected() {
		assertSharedVariantSelected(DEFAULT, DEFAULT)
	}

	@Override
	protected void assertStaticVariantSelected() {
		assertStaticVariantSelected(DEFAULT, DEFAULT)
	}

	@Override
	protected void assertFrameworkVariantSelected() {
		assertFrameworkVariantSelected(DEFAULT, DEFAULT)
	}
}
