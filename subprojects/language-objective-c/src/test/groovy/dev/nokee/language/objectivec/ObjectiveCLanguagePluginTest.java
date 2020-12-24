package dev.nokee.language.objectivec;

import dev.nokee.internal.testing.testers.WellBehavedPluginTester;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetExtensible;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguagePlugin;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.gradle.api.Plugin;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.testing.utils.TestUtils.rootProject;
import static dev.nokee.model.internal.type.ModelType.of;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(ObjectiveCLanguagePlugin.class)
class ObjectiveCLanguagePluginTest extends WellBehavedPluginTester {
	@Override
	protected String getQualifiedPluginIdUnderTest() {
		return "dev.nokee.objective-c-language";
	}

	@Override
	protected Class<? extends Plugin<?>> getPluginTypeUnderTest() {
		return ObjectiveCLanguagePlugin.class;
	}

	@Test
	void appliesCppLanguageBasePlugin() {
		val project = rootProject();
		project.apply(singletonMap("plugin", ObjectiveCLanguagePlugin.class));
		assertTrue(project.getPlugins().hasPlugin(ObjectiveCLanguageBasePlugin.class), "should apply Objective-C language base plugin");
	}

	@Test
	void createsObjectiveCSourceSetUponDiscoveringSourceSetExtensibleType() {
		val project = rootProject();
		project.apply(singletonMap("plugin", ObjectiveCLanguagePlugin.class));
		val modelRegistry = project.getExtensions().getByType(ModelRegistry.class);
		modelRegistry.register(NodeRegistration.of("test", of(ObjectiveCSourceSetExtensible.class)));
		assertDoesNotThrow(() -> modelRegistry.get("test.objectiveC", ObjectiveCSourceSet.class));
	}
}
