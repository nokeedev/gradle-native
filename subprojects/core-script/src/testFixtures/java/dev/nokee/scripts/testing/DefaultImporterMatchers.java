package dev.nokee.scripts.testing;

import org.gradle.api.Project;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

public final class DefaultImporterMatchers {
	private DefaultImporterMatchers() {}

	public static Matcher<Project> hasDefaultImportFor(Class<?> type) {
		return new TypeSafeMatcher<Project>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("default import of " + type);
			}

			@Override
			protected boolean matchesSafely(Project item) {
				return Objects.equals(item.getExtensions().findByName(type.getSimpleName()), type);
			}
		};
	}
}
