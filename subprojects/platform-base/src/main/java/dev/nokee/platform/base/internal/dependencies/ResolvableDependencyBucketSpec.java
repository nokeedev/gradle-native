/*
 * Copyright 2022 the original author or authors.
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

import dev.nokee.model.DependencyFactory;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectRegistry;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;

import javax.inject.Inject;

public /*final*/ abstract class ResolvableDependencyBucketSpec extends ModelElementSupport implements ResolvableDependencyBucket
	, DependencyBucketMixIn
{
	private final IncomingArtifacts incoming;
	private final DependencyFactory factory;

	@Inject
	public ResolvableDependencyBucketSpec(DependencyHandler handler, ModelObjectRegistry<Configuration> configurationRegistry) {
		getExtensions().add("$configuration", configurationRegistry.register(getIdentifier(), Configuration.class).get());

		this.factory = DependencyFactory.forHandler(handler);
		this.incoming = new IncomingArtifacts(getAsConfiguration());
	}

	@Override
	public FileCollection getAsLenientFileCollection() {
		return incoming.getAsLenient();
	}

	@Override
	public FileCollection getAsFileCollection() {
		return incoming.get();
	}

	@Override
	public DependencyFactory getDependencyFactory() {
		return factory;
	}

	@Override
	public String toString() {
		return "resolvable dependency bucket '" + getName() + "'";
	}
}
