package dev.nokee.platform.jni.dependencies

class ComponentWithBuildTypesConsumingNokeeProducerFunctionalTest extends AbstractNokeeConfigurationFunctionalTest {
	@Override
	protected void reportDependencies() {
		reportDependencies(debug, release)
	}

	@Override
	protected void makeSingleProject() {
		makeSingleProject(debug, release)
	}

	@Override
	protected void assertStaticVariantSelectedMatchingBuildTypes(BuildType... producer) {
		assertStaticVariantSelected(debug, debug)
		assertStaticVariantSelected(release, release)
	}

	@Override
	protected void assertSharedVariantSelectedMatchingBuildTypes(BuildType... producer) {
		assertSharedVariantSelected(debug, debug)
		assertSharedVariantSelected(release, release)
	}

	@Override
	protected void assertFrameworkVariantSelectedMatchingBuildTypes(BuildType... producer) {
		assertFrameworkVariantSelected(debug, debug)
		assertFrameworkVariantSelected(release, release)
	}

	@Override
	protected void assertSharedVariantSelected() {
		assertSharedVariantSelected(debug, DEFAULT)
		assertSharedVariantSelected(release, DEFAULT)
	}

	@Override
	protected void assertStaticVariantSelected() {
		assertStaticVariantSelected(debug, DEFAULT)
		assertStaticVariantSelected(release, DEFAULT)
	}

	@Override
	protected void assertFrameworkVariantSelected() {
		assertFrameworkVariantSelected(debug, DEFAULT)
		assertFrameworkVariantSelected(release, DEFAULT)
	}
}
