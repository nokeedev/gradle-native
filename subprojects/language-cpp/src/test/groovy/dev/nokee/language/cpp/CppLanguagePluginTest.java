package dev.nokee.language.cpp;

import dev.nokee.internal.testing.testers.WellBehavedPluginTester;
import dev.nokee.language.cpp.internal.CppSourceSetExtensible;
import dev.nokee.language.cpp.internal.plugins.CppLanguageBasePlugin;
import dev.nokee.language.cpp.internal.plugins.CppLanguagePlugin;
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

@Subject(CppLanguagePlugin.class)
class CppLanguagePluginTest extends WellBehavedPluginTester {
	@Override
	protected String getQualifiedPluginIdUnderTest() {
		return "dev.nokee.cpp-language";
	}

	@Override
	protected Class<? extends Plugin<?>> getPluginTypeUnderTest() {
		return CppLanguagePlugin.class;
	}

	@Test
	void appliesCppLanguageBasePlugin() {
		val project = rootProject();
		project.apply(singletonMap("plugin", CppLanguagePlugin.class));
		assertTrue(project.getPlugins().hasPlugin(CppLanguageBasePlugin.class), "should apply C++ language base plugin");
	}

	@Test
	void createsCppSourceSetUponDiscoveringSourceSetExtensibleType() {
		val project = rootProject();
		project.apply(singletonMap("plugin", CppLanguagePlugin.class));
		val modelRegistry = project.getExtensions().getByType(ModelRegistry.class);
		modelRegistry.register(NodeRegistration.of("test", of(CppSourceSetExtensible.class)));
		assertDoesNotThrow(() -> modelRegistry.get("test.cpp", CppSourceSet.class));
	}
}
