package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Subject.ofName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

@Subject(ConfigurationDescription.Subject.class)
class ConfigurationDescriptionSubjectTest {
	@Test
	void singleWordName() {
		assertThat(ofName("implementation"), hasToString("implementation"));
	}

	@Test
	void twoWordsName() {
		assertThat(ofName("compileOnly"), hasToString("compile only"));
		assertThat(ofName("linkOnly"), hasToString("link only"));
		assertThat(ofName("runtimeOnly"), hasToString("runtime only"));
		assertThat(ofName("runtimeElements"), hasToString("runtime elements"));
		assertThat(ofName("compileElements"), hasToString("compile elements"));
	}

	@Test
	void threeWordsName() {
		assertThat(ofName("headerSearchPaths"), hasToString("header search paths"));
		assertThat(ofName("importSwiftModules"), hasToString("import swift modules"));
	}

	@Test
	void alwaysCapitalizeApi() {
		assertThat(ofName("api"), hasToString("API"));
		assertThat(ofName("apiElements"), hasToString("API elements"));
		assertThat(ofName("jvmApiElements"), hasToString("jvm API elements"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(ConfigurationDescription.Subject.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(ofName("foo"), ofName("foo"))
			.addEqualityGroup(ofName("api"), ofName("API"))
			.addEqualityGroup(ofName("bar"))
			.testEquals();
	}
}
