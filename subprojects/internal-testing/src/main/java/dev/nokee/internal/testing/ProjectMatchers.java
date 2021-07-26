package dev.nokee.internal.testing;

import org.gradle.api.plugins.PluginAware;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static java.util.Objects.requireNonNull;

public final class ProjectMatchers {
	private ProjectMatchers() {}

	public static Matcher<PluginAware> hasPlugin(String id) {
		requireNonNull(id);
		return new TypeSafeMatcher<PluginAware>() {
			@Override
			protected boolean matchesSafely(PluginAware actual) {
				return actual.getPluginManager().hasPlugin(id);
			}

			@Override
			protected void describeMismatchSafely(PluginAware item, Description description) {
				description.appendText("plugin ").appendValue(id).appendText(" is not applied to ").appendValue(item);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("a plugin aware object has plugin ").appendValue(id).appendText(" applied");
			}
		};
	}
}
