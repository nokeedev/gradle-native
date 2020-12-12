package dev.nokee.model.internal.plugins;

import dev.nokee.internal.testing.testers.WellBehavedPluginTester;
import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.MatcherAssert.assertThat;

@Subject(ModelBasePlugin.class)
class ModelBasePluginTest {
	@Nested
	class IsWellBehavingPlugin extends WellBehavedPluginTester {
		@Override
		protected String getQualifiedPluginIdUnderTest() {
			Assumptions.assumeTrue(false, "plugin does not have a plugin id");
			throw new UnsupportedOperationException();
		}

		@Override
		protected Class<? extends Plugin<?>> getPluginTypeUnderTest() {
			return ModelBasePlugin.class;
		}
	}

	private final Project project = TestUtils.rootProject();

	@Test
	void registersEventPublisherService() {
		project.apply(of("plugin", ModelBasePlugin.class));
		assertThat(project, hasExtensionOf(DomainObjectEventPublisher.class));
	}

	@Test
	void registersRealizableService() {
		project.apply(of("plugin", ModelBasePlugin.class));
		assertThat(project, hasExtensionOf(RealizableDomainObjectRealizer.class));
	}

	@Test
	void registersModelRegistryService() {
		project.apply(of("plugin", ModelBasePlugin.class));
		assertThat(project, hasExtensionOf(ModelRegistry.class));
	}

	private static Matcher<Project> hasExtensionOf(Class<?> extensionType) {
		return new TypeSafeMatcher<Project>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("extension of type " + extensionType);
			}

			@Override
			protected boolean matchesSafely(Project item) {
				return item.getExtensions().findByType(extensionType) != null;
			}
		};
	}
}
