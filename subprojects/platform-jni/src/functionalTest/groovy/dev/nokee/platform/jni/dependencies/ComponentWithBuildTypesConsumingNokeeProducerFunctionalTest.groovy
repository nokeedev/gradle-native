/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
