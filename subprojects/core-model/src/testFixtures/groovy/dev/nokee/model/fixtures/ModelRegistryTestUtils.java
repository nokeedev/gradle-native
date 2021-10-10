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

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import org.gradle.api.model.ObjectFactory;

import java.io.File;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelPath.path;

public class ModelRegistryTestUtils {
	private static final DefaultModelRegistry TEST_MODEL_REGISTRY = new DefaultModelRegistry(objectFactory()::newInstance);
	private static int count = 0;

	public static ModelElement create(NodeRegistration registration) {
		return create(TEST_MODEL_REGISTRY, registration);
	}

	public static ModelElement create(DefaultModelRegistry modelRegistry, NodeRegistration registration) {
		String testHolderName = String.valueOf(count++);
		modelRegistry.register(ModelRegistration.of(testHolderName, (TestHolder.class)));
		return ModelNodeUtils.register(modelRegistry.get(path(testHolderName)), registration);
	}

	public static DefaultModelRegistry registry(File baseDirectory) {
		return new DefaultModelRegistry(ProjectTestUtils.createRootProject(baseDirectory).getObjects()::newInstance);
	}

	public static DefaultModelRegistry registry(ObjectFactory objectFactory) {
		return new DefaultModelRegistry(objectFactory::newInstance);
	}

	interface TestHolder {}
}
