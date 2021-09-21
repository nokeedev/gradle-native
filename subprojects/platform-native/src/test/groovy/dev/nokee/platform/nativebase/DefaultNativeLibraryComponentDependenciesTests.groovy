/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.nativebase

import dev.nokee.fixtures.AbstractComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLibraryComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesIntegrationTest
import dev.nokee.model.internal.DomainObjectIdentifierInternal
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory
import spock.lang.Subject

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory

@Subject(DefaultNativeLibraryComponentDependencies)
class DefaultNativeLibraryComponentDependenciesIntegrationTest extends AbstractComponentDependenciesIntegrationTest {
	@Override
	protected newDependencies(DomainObjectIdentifierInternal identifier) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(objectFactory(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
		return project.objects.newInstance(DefaultNativeLibraryComponentDependencies, dependencyContainer)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['api', 'implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

@Subject(DefaultNativeLibraryComponentDependencies)
class DefaultNativeLibraryComponentDependenciesLocalDarwinFrameworkIntegrationTest extends AbstractLocalDarwinFrameworkDependenciesIntegrationTest {
	@Override
	protected newDependencies(DomainObjectIdentifierInternal identifier) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(objectFactory(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
		return project.objects.newInstance(DefaultNativeLibraryComponentDependencies, dependencyContainer)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['api', 'implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

@Subject(DefaultNativeLibraryComponentDependencies)
class DefaultNativeLibraryComponentDependenciesApiBucketIntegrationTest extends AbstractLibraryComponentDependenciesIntegrationTest {
	@Override
	protected newDependencies(DomainObjectIdentifierInternal identifier) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(objectFactory(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
		return project.objects.newInstance(DefaultNativeLibraryComponentDependencies, dependencyContainer)
	}

	@Override
	protected String getApiBucketNameUnderTest() {
		return 'api'
	}

	@Override
	protected String getImplementationBucketNameUnderTest() {
		return 'implementation'
	}
}
