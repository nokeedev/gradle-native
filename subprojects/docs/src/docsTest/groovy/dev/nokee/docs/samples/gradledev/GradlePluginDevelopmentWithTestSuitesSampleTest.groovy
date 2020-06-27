package dev.nokee.docs.samples.gradledev

import dev.nokee.docs.samples.WellBehavingSampleTest

class GradlePluginDevelopmentWithTestSuitesSampleTest extends WellBehavingSampleTest {
	@Override
	protected String getSampleName() {
		return 'gradle-plugin-development-with-test-suites'
	}

	List<String> getExpectedAdditionalExtensions() {
		return ['groovy'] // for spock tests
	}
}
