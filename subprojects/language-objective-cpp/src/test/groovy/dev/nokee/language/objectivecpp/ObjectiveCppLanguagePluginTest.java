package dev.nokee.language.objectivecpp;

import dev.nokee.internal.testing.testers.WellBehavedPluginTester;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetExtensible;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguageBasePlugin;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguagePlugin;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.gradle.api.Plugin;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.model.internal.type.ModelType.of;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(ObjectiveCppLanguagePlugin.class)
class ObjectiveCppLanguagePluginTest extends WellBehavedPluginTester {
	@Override
	protected String getQualifiedPluginIdUnderTest() {
		return "dev.nokee.objective-cpp-language";
	}

	@Override
	protected Class<? extends Plugin<?>> getPluginTypeUnderTest() {
		return ObjectiveCppLanguagePlugin.class;
	}

	@Test
	void appliesCppLanguageBasePlugin() {
		val project = rootProject();
		project.apply(singletonMap("plugin", ObjectiveCppLanguagePlugin.class));
		assertTrue(project.getPlugins().hasPlugin(ObjectiveCppLanguageBasePlugin.class), "should apply Objective-C++ language base plugin");
	}

	@Test
	void createsObjectiveCppSourceSetUponDiscoveringSourceSetExtensibleType() {
		val project = rootProject();
		project.apply(singletonMap("plugin", ObjectiveCppLanguagePlugin.class));
		val modelRegistry = project.getExtensions().getByType(ModelRegistry.class);
		modelRegistry.register(NodeRegistration.of("test", of(ObjectiveCppSourceSetExtensible.class)));
		assertDoesNotThrow(() -> modelRegistry.get("test.objectiveCpp", ObjectiveCppSourceSet.class));
	}
}
