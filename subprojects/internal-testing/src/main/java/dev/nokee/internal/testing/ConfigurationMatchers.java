package dev.nokee.internal.testing;

import com.google.common.collect.ImmutableMap;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.*;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.HasAttributes;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import javax.annotation.Nullable;
import java.io.File;
import java.util.AbstractMap;
import java.util.Map;

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
	 * Matches extends from of the configuration.
	 *
	 * @param matcher  an extends from matcher, must not be null
	 * @return a configuration matcher for its extends from, never null
	 */
	public static Matcher<Configuration> extendsFrom(Matcher<? super Iterable<Configuration>> matcher) {
		return new FeatureMatcher<Configuration, Iterable<Configuration>>(requireNonNull(matcher), "configuration's extends from is", "configuration's extends from") {
			@Override
			protected Iterable<Configuration> featureValueOf(Configuration actual) {
				return actual.getExtendsFrom();
			}
		};
	}

	/**
	 * Matches a attributes of the configuration.
	 *
	 * @param matcher  matcher for all of the configuration's attributes, must not be null
	 * @return a configuration matcher for its attributes, never null
	 */
	public static Matcher<HasAttributes> attributes(Matcher<? super Map<? extends Attribute<?>, ?>> matcher) {
		return new FeatureMatcher<HasAttributes, Map<Attribute<?>, ?>>(matcher, "a configuration with attribute", "attributes") {
			@Override
			protected Map<Attribute<?>, ?> featureValueOf(HasAttributes actual) {
				return actual.getAttributes().keySet().stream().map(attribute -> new AbstractMap.SimpleImmutableEntry<>(attribute, actual.getAttributes().getAttribute(attribute))).collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
			}
		};
	}

	/**
	 * Matches an attribute for key and value matchers.
	 *
	 * @param keyMatcher  the attribute key matcher, must not be null
	 * @param valueMatcher  the attribute value matcher, must not be null
	 * @param <T>  the attribute type
	 * @return an attribute container matcher for the specified key/value matchers, never null
	 */
	public static <T> Matcher<AttributeContainer> hasAttribute(Matcher<? super Attribute<T>> keyMatcher, Matcher<? super T> valueMatcher) {
		return new FeatureMatcher<AttributeContainer, Map<? extends Attribute<?>, ?>>(hasEntry((Matcher<? super Attribute<?>>) keyMatcher, (Matcher<? super Object>) valueMatcher), "an attribute", "the attribute") {
			@Override
			protected Map<? extends Attribute<?>, ?> featureValueOf(AttributeContainer actual) {
				return actual.getAttributes().keySet().stream().map(attribute -> new AbstractMap.SimpleImmutableEntry<>(attribute, actual.getAttributes().getAttribute(attribute))).collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
			}
		};
	}

	/**
	 * Matches an attribute key.
	 *
	 * @param attribute  the attribute key, must not be null
	 * @param <T>  the attribute type
	 * @return an attribute container matcher for the specified key, never null
	 */
	public static <T> Matcher<AttributeContainer> hasAttribute(Attribute<T> attribute) {
		return new FeatureMatcher<AttributeContainer, Map<? extends Attribute<?>, ?>>(hasKey(equalTo(attribute)), "an attribute", "the attribute") {
			@Override
			protected Map<? extends Attribute<?>, ?> featureValueOf(AttributeContainer actual) {
				return actual.getAttributes().keySet().stream().map(attribute -> new AbstractMap.SimpleImmutableEntry<>(attribute, actual.getAttributes().getAttribute(attribute))).collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
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

	/**
	 * Matches a declarable configuration, that is a configuration where {@link Configuration#isCanBeConsumed()} is {@literal false} and {@link Configuration#isCanBeResolved()} is {@literal false}.
	 *
	 * @return a configuration matcher, never null
	 */
	public static Matcher<Configuration> declarable() {
		return new TypeSafeMatcher<Configuration>() {
			@Override
			protected boolean matchesSafely(Configuration item) {
				return !item.isCanBeConsumed() && !item.isCanBeResolved();
			}

			@Override
			protected void describeMismatchSafely(Configuration item, Description description) {
				description.appendText("was a ").appendText(configurationType(item)).appendText(" configuration");
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("a declarable configuration");
			}
		};
	}

	/**
	 * Matches a consumable configuration, that is a configuration where {@link Configuration#isCanBeConsumed()} is {@literal true} and {@link Configuration#isCanBeResolved()} is {@literal false}.
	 *
	 * @return a configuration matcher, never null
	 */
	public static Matcher<Configuration> consumable() {
		return new TypeSafeMatcher<Configuration>() {
			@Override
			protected boolean matchesSafely(Configuration item) {
				return item.isCanBeConsumed() && !item.isCanBeResolved();
			}

			@Override
			protected void describeMismatchSafely(Configuration item, Description description) {
				description.appendText("was a ").appendText(configurationType(item)).appendText(" configuration");
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("a consumable configuration");
			}
		};
	}

	/**
	 * Matches a resolvable configuration, that is a configuration where {@link Configuration#isCanBeConsumed()} is {@literal false} and {@link Configuration#isCanBeResolved()} is {@literal true}.
	 *
	 * @return a configuration matcher, never null
	 */
	public static Matcher<Configuration> resolvable() {
		return new TypeSafeMatcher<Configuration>() {
			@Override
			protected boolean matchesSafely(Configuration item) {
				return !item.isCanBeConsumed() && item.isCanBeResolved();
			}

			@Override
			protected void describeMismatchSafely(Configuration item, Description description) {
				description.appendText("was a ").appendText(configurationType(item)).appendText(" configuration");
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("a resolvable configuration");
			}
		};
	}

	private static String configurationType(Configuration item) {
		if (item.isCanBeConsumed() && item.isCanBeResolved()) {
			return "legacy";
		} else if (item.isCanBeConsumed() && !item.isCanBeResolved()) {
			return "consumable";
		} else if (!item.isCanBeConsumed() && item.isCanBeResolved()) {
			return "resolvable";
		} else if (!item.isCanBeConsumed() && !item.isCanBeResolved()) {
			return "declarable";
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * Matches a configuration description using the specified matcher.
	 *
	 * @param matcher  a configuration description to matcher, must not be null
	 * @return a configuration matcher, never null
	 */
	public static Matcher<Configuration> description(Matcher<? super String> matcher) {
		return new FeatureMatcher<Configuration, String>(matcher, "a configuration with description of", "configuration description") {
			@Override
			protected String featureValueOf(Configuration actual) {
				return actual.getDescription();
			}
		};
	}
}
