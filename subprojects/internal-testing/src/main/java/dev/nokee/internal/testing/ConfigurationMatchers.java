package dev.nokee.internal.testing;

import lombok.val;
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

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
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
		return configurations(hasItem(requireNonNull(matcher)));
	}

	public static Matcher<Project> configurations(Matcher<? super Iterable<Configuration>> matcher) {
		return new FeatureMatcher<Project, Iterable<Configuration>>(requireNonNull(matcher), "project's configurations is", "project's configurations") {
			@Override
			protected Iterable<Configuration> featureValueOf(Project actual) {
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
	public static Matcher<Object> hasPublishArtifact(Matcher<? super PublishArtifact> matcher) {
		return new FeatureMatcher<Object, PublishArtifactSet>(hasItem(requireNonNull(matcher)), "a configuration with", "configuration") {
			@Override
			protected PublishArtifactSet featureValueOf(Object actual) {
				assertThat(actual, anyOf(isA(Configuration.class), isA(ConfigurationVariant.class)));
				if (actual instanceof Configuration) {
					return ((Configuration) actual).getArtifacts();
				} else if (actual instanceof ConfigurationVariant) {
					return ((ConfigurationVariant) actual).getArtifacts();
				}
				throw new UnsupportedOperationException();
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
		return ofFile(equalTo(requireNonNull(file)));
	}

	public static Matcher<PublishArtifact> ofFile(Matcher<? super File> matcher) {
		return new FeatureMatcher<PublishArtifact, File>(requireNonNull(matcher), "a publish artifact with file", "publish artifact's file") {
			@Override
			protected File featureValueOf(PublishArtifact actual) {
				return actual.getFile();
			}
		};
	}

	public static Matcher<PublishArtifact> ofClassifier(String classifier) {
		return ofClassifier(equalTo(requireNonNull(classifier)));
	}

	public static Matcher<PublishArtifact> ofClassifier(Matcher<? super String> matcher) {
		return new FeatureMatcher<PublishArtifact, String>(requireNonNull(matcher), "a publish artifact with classifier", "publish artifact's classifier") {
			@Nullable
			@Override
			protected String featureValueOf(PublishArtifact actual) {
				return actual.getClassifier();
			}
		};
	}

	public static Matcher<PublishArtifact> ofType(String type) {
		return new FeatureMatcher<PublishArtifact, String>(equalTo(requireNonNull(type)), "a publish artifact with type", "publish artifact's type") {
			@Override
			protected String featureValueOf(PublishArtifact actual) {
				return actual.getType();
			}
		};
	}

	public static Matcher<Configuration> hasOutgoingVariant(Matcher<? super ConfigurationVariant> matcher) {
		return outgoingVariants(hasItem(matcher));
	}

	/**
	 * Matches outgoing variants of the configuration.
	 *
	 * @param matcher  an outgoing variant matcher, must not be null
	 * @return a configuration matcher for its outgoing variants, never null
	 */
	public static Matcher<Configuration> outgoingVariants(Matcher<? super Iterable<ConfigurationVariant>> matcher) {
		return new FeatureMatcher<Configuration, Iterable<ConfigurationVariant>>(requireNonNull(matcher), "configuration's outgoing variants is", "configuration's outgoing variants") {
			@Override
			protected Iterable<ConfigurationVariant> featureValueOf(Configuration actual) {
				return actual.getOutgoing().getVariants();
			}
		};
	}

	/**
	 * Matches dependencies of the configuration.
	 *
	 * @param matcher  a dependency matcher, must not be null
	 * @return a configuration matcher for its dependency, never null
	 */
	public static Matcher<Configuration> dependencies(Matcher<? super Iterable<Dependency>> matcher) {
		return new FeatureMatcher<Configuration, Iterable<Dependency>>(matcher, "configuration's dependencies is", "configuration's dependencies") {
			@Override
			protected Iterable<Dependency> featureValueOf(Configuration actual) {
				return actual.getDependencies();
			}
		};
	}

	/**
	 * Matches a attributes of the configuration.
	 *
	 * @param matcher  matcher for all of the configuration's attributes, must not be null
	 * @return a configuration matcher for its attributes, never null
	 */
	public static Matcher<Configuration> attributes(Matcher<Map<? extends Attribute<?>, ?>> matcher) {
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
