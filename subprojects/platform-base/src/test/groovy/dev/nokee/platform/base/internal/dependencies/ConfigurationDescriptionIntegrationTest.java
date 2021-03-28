package dev.nokee.platform.base.internal.dependencies;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.function.Supplier;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.createChildProject;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.platform.base.internal.ComponentName.of;
import static dev.nokee.platform.base.internal.ComponentName.ofMain;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Bucket.*;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Owner.*;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Subject.ofName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Subject(ConfigurationDescription.class)
class ConfigurationDescriptionIntegrationTest {
	@Test
	void descriptionOfConfigurationOwnedByProject() {
		assertThat(description(ofName("implementation"), ofDeclarable(), ofProject(rootProject())),
			supplierOf(equalTo("Implementation dependencies for project ':'.")));
		assertThat(description(ofName("headerSearchPaths"), ofConsumable(), ofProject(createChildProject(rootProject(), "bar"))),
			supplierOf(equalTo("Header search paths for project ':bar'.")));
		assertThat(description(ofName("apiElements"), ofResolvable(), ofProject(createChildProject(rootProject(), "far"))),
			supplierOf(equalTo("API elements for project ':far'.")));
	}

	@Test
	void descriptionOfConfigurationOwnedByComponent() {
		assertThat(description(ofName("compileOnly"), ofDeclarable(), ofComponent(ofMain())),
			supplierOf(equalTo("Compile only dependencies for main component.")));
		assertThat(description(ofName("importSwiftModules"), ofConsumable(), ofComponent(of("test"))),
			supplierOf(equalTo("Import swift modules for component 'test'.")));
		assertThat(description(ofName("compileElements"), ofResolvable(), ofComponent(of("common"))),
			supplierOf(equalTo("Compile elements for component 'common'.")));
	}

	@Test
	void descriptionOfConfigurationOwnedByVariant() {
		assertThat(description(ofName("runtimeOnly"), ofDeclarable(), ofVariant(ofMain(), "debug")),
			supplierOf(equalTo("Runtime only dependencies for variant 'debug'.")));
		assertThat(description(ofName("linkLibraries"), ofConsumable(), ofVariant(of("test"), "x86-64Release")),
			supplierOf(equalTo("Link libraries for variant 'x86-64Release' of component 'test'.")));
		assertThat(description(ofName("runtimeElements"), ofResolvable(), ofVariant(of("lib"), "")),
			supplierOf(equalTo("Runtime elements for component 'lib'.")));
	}

	private static ConfigurationDescription description(ConfigurationDescription.Subject subject, ConfigurationDescription.Bucket bucket, ConfigurationDescription.Owner owner) {
		return new ConfigurationDescription(subject, bucket, owner);
	}

	private static <T> Matcher<Supplier<T>> supplierOf(Matcher<? super T> matcher) {
		return new FeatureMatcher<Supplier<T>, T>(matcher, "supplier of", "supply") {
			@Override
			protected T featureValueOf(Supplier<T> actual) {
				return actual.get();
			}
		};
	}
}
