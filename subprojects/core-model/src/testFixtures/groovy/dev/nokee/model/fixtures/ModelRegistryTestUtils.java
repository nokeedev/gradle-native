package dev.nokee.model.fixtures;

import dev.gradleplugins.grava.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import org.gradle.api.model.ObjectFactory;

import java.io.File;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelPath.path;

public class ModelRegistryTestUtils {
	private static final DefaultModelRegistry TEST_MODEL_REGISTRY = new DefaultModelRegistry(objectFactory()::newInstance);
	private static int count = 0;

	public static <T> T create(NodeRegistration<T> registration) {
		return create(TEST_MODEL_REGISTRY, registration);
	}

	public static <T> T create(DefaultModelRegistry modelRegistry, NodeRegistration<T> registration) {
		String testHolderName = String.valueOf(count++);
		modelRegistry.register(ModelRegistration.of(testHolderName, (TestHolder.class)));
		return modelRegistry.get(path(testHolderName)).register(registration).get();
	}

	public static DefaultModelRegistry registry(File baseDirectory) {
		return new DefaultModelRegistry(ProjectTestUtils.createRootProject(baseDirectory).getObjects()::newInstance);
	}

	public static DefaultModelRegistry registry(ObjectFactory objectFactory) {
		return new DefaultModelRegistry(objectFactory::newInstance);
	}

	interface TestHolder {}
}
