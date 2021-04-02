package dev.nokee.language.c;

import dev.nokee.language.c.internal.CSourceSetExtensible;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.c.internal.plugins.CLanguagePlugin;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.model.internal.type.ModelType.of;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(CLanguagePlugin.class)
class CLanguagePluginTest {
	@Test
	void appliesCLanguageBasePlugin() {
		val project = rootProject();
		project.apply(singletonMap("plugin", CLanguagePlugin.class));
		assertTrue(project.getPlugins().hasPlugin(CLanguageBasePlugin.class), "should apply C language base plugin");
	}

	@Test
	void createsCSourceSetUponDiscoveringSourceSetExtensibleType() {
		val project = rootProject();
		project.apply(singletonMap("plugin", CLanguagePlugin.class));
		val modelRegistry = project.getExtensions().getByType(ModelRegistry.class);
		modelRegistry.register(NodeRegistration.of("test", of(CSourceSetExtensible.class)));
		assertDoesNotThrow(() -> modelRegistry.get("test.c", CSourceSet.class));
	}
}
