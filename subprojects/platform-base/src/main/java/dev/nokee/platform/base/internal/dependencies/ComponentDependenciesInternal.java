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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.DependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import java.util.Optional;

public interface ComponentDependenciesInternal {
	DomainObjectIdentifierInternal getOwnerIdentifier(); // Only until we finish the refactoring
	DependencyBucket add(DependencyBucket bucket);
	DependencyBucket create(String name, Action<Configuration> action) throws InvalidUserDataException;
	DependencyBucket getByName(String name) throws UnknownDomainObjectException;
	void add(String bucketName, Object notation);
	void add(String bucketName, Object notation, Action<? super ModuleDependency> action);
	void configureEach(Action<? super DependencyBucket> action);
	Optional<DependencyBucket> findByName(String name);
}
