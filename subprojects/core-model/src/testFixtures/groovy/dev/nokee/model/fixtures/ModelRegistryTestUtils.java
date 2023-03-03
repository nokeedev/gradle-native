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
package dev.nokee.model.fixtures;

import com.google.common.collect.Streams;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import lombok.EqualsAndHashCode;
import org.gradle.api.model.ObjectFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;

public class ModelRegistryTestUtils {
	private static final DefaultModelRegistry TEST_MODEL_REGISTRY = new DefaultModelRegistry(objectFactory()::newInstance);
	private static int count = 0;

	public static DefaultModelRegistry registry(File baseDirectory) {
		return new DefaultModelRegistry(ProjectTestUtils.createRootProject(baseDirectory).getObjects()::newInstance);
	}

	public static DefaultModelRegistry registry(ObjectFactory objectFactory) {
		return new DefaultModelRegistry(objectFactory::newInstance);
	}

	public static DomainObjectIdentifier identifier(String path) {
		return new Identifier(Streams.stream(ModelPath.path(path)).map(Identity::new).toArray(Object[]::new));
	}

	@EqualsAndHashCode
	private static final class Identifier implements DomainObjectIdentifier {
		@EqualsAndHashCode.Include private final Iterable<Object> identities;

		public Identifier(Object... identities) {
			this.identities = Arrays.asList(identities);
		}

		@Override
		public Iterator<Object> iterator() {
			return identities.iterator();
		}
	}

	@EqualsAndHashCode
	private static final class Identity implements HasName {
		@EqualsAndHashCode.Include private final String name;

		public Identity(String name) {
			this.name = name;
		}

		@Override
		public Object getName() {
			return name;
		}
	}

	interface TestHolder {}
}
