package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.artifacts.*;
import org.gradle.api.attributes.Attribute;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import javax.annotation.Nullable;
import java.io.File;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.*;

public final class ConfigurationMatchers {
	private ConfigurationMatchers() {}

	/**
	 * Matches a configuration of the project.
	 *
	 * @param matcher  a configuration matcher, must not be null
	 * @return a project matcher for its configurations, never null
	 */
	public static Matcher<Project> hasConfiguration(Matcher<? super Configuration> matcher) {
		return new FeatureMatcher<Project, ConfigurationContainer>(hasItem(requireNonNull(matcher)), "", "") {
			@Override
			protected ConfigurationContainer featureValueOf(Project actual) {
				return actual.getConfigurations();
			}
		};
	}

	/**
	 * Matches a publish artifact of a configuration.
	 *
	 * @param matcher  the publish artifact matcher, must not be null
	 * @return a configuration matcher for its publish artifact, never null
	 */
	public static Matcher<Configuration> hasPublishArtifact(Matcher<? super PublishArtifact> matcher) {
		return new FeatureMatcher<Configuration, PublishArtifactSet>(hasItem(requireNonNull(matcher)), "a configuration with", "a") {
			@Override
			protected PublishArtifactSet featureValueOf(Configuration actual) {
				return actual.getArtifacts();
			}
		};
	}

	/**
	 * Matches a publish artifact by file.
	 *
	 * @param file  the file of the artifact, must not be null
	 * @return a publish artifact matcher for its file, never null
	 */
	public static Matcher<PublishArtifact> ofFile(File file) {
		return new FeatureMatcher<PublishArtifact, File>(equalTo(requireNonNull(file)), "a publish artifact with file", "b") {
			@Override
			protected File featureValueOf(PublishArtifact actual) {
				return actual.getFile();
			}
		};
	}

	/**
	 * Matches a dependency of the configuration.
	 *
	 * @param matcher  a dependency matcher, must not be null
	 * @return a configuration matcher for its dependency, never null
	 */
	public static Matcher<Configuration> hasDependency(Matcher<? super Dependency> matcher) {
		return new FeatureMatcher<Configuration, DependencySet>(hasItem(requireNonNull(matcher)), "has dependency", "has dependency") {
			@Override
			protected DependencySet featureValueOf(Configuration actual) {
				return actual.getDependencies();
			}
		};
	}

	/**
	 * Matches an attribute of the configuration.
	 *
	 * @param attribute  the attribute, must not be null
	 * @param value  the attribute value, must not be null
	 * @param <T>  the attribute type
	 * @return a configuration matcher for its attribute, never null
	 */
	public static <T> Matcher<Configuration> hasAttribute(Attribute<T> attribute, T value) {
		return attributes(hasEntry(requireNonNull(attribute), requireNonNull(value)));
	}

	/**
	 * Matches an attribute of the configuration.
	 *
	 * @param attribute  the attribute, must not be null
	 * @param valueMatcher  the attribute value matcher, must not be null
	 * @param <T>  the attribute type
	 * @return a configuration matcher for its attributes, never null
	 */
	@SuppressWarnings("unchecked")
	public static <T> Matcher<Configuration> hasAttribute(Attribute<T> attribute, Matcher<? super T> valueMatcher) {
		return attributes(hasEntry(equalTo(requireNonNull(attribute)), requireNonNull((Matcher<Object>) valueMatcher)));
	}

	/**
	 * Matches a named attribute of the configuration.
	 *
	 * @param attribute  the attribute, must not be null
	 * @param name  the attribute name, must not be null
	 * @param <T>  the attribute type
	 * @return a configuration matcher for its attributes, never null
	 */
	public static <T extends Named> Matcher<Configuration> hasAttribute(Attribute<T> attribute, String name) {
		return attributes(hasEntry(equalTo(requireNonNull(attribute)), named(name)));
	}

	private static Matcher<Configuration> attributes(Matcher<Map<? extends Attribute<?>, ?>> matcher) {
		return new FeatureMatcher<Configuration, Map<Attribute<?>, ?>>(matcher, "a configuration with attribute", "attributes") {
			@Override
			protected Map<Attribute<?>, ?> featureValueOf(Configuration actual) {
				return actual.getAttributes().keySet().stream().map(attribute -> new AbstractMap.SimpleImmutableEntry<>(attribute, actual.getAttributes().getAttribute(attribute))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			}
		};
	}

	/**
	 * Matches a dependency with the specified coordinate.
	 *
	 * @param coordinate  the expected dependency coordinate, must not be null
	 * @return a dependency matcher, never null
	 */
	public static <T extends Dependency> Matcher<T> forCoordinate(String coordinate) {
		val token = coordinate.split(":");
		return forCoordinate(token[0], token[1], token[2]);
	}

	/**
	 * Matches a dependency with the specified coordinate.
	 *
	 * @param group  the expected dependency group, can be null
	 * @param name  the expected dependency name, must not be null
	 * @param version  the expected dependency version, can be null
	 * @return a dependency matcher, never null
	 */
	public static <T extends Dependency> Matcher<T> forCoordinate(@Nullable String group, String name, @Nullable String version) {
		return allOf(withGroup(equalTo(group)), withName(equalTo(requireNonNull(name))), withVersion(equalTo(version)));
	}

	private static Matcher<Dependency> withGroup(Matcher<? super String> matcher) {
		return new FeatureMatcher<Dependency, String>(matcher, "a dependency with group", "but dependency's group") {
			@Nullable
			@Override
			protected String featureValueOf(Dependency actual) {
				return actual.getGroup();
			}
		};
	}

	private static Matcher<Dependency> withName(Matcher<? super String> matcher) {
		return new FeatureMatcher<Dependency, String>(matcher, "a dependency with name", "but dependency's name") {
			@Override
			protected String featureValueOf(Dependency actual) {
				return actual.getName();
			}
		};
	}

	private static Matcher<Dependency> withVersion(Matcher<? super String> matcher) {
		return new FeatureMatcher<Dependency, String>(matcher, "a dependency with version", "but dependency's version") {
			@Nullable
			@Override
			protected String featureValueOf(Dependency actual) {
				return actual.getVersion();
			}
		};
	}
}
