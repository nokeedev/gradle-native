package dev.nokee.runtime.nativebase;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.runtime.nativebase.BuildType.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class BuildTypeTest {
	@Nested
	class ObjectFactoryTest implements NamedValueTester<BuildType> {
		@Override
		public BuildType createSubject(String name) {
			return objectFactory().named(BuildType.class, name);
		}
	}

	@Nested
	class MethodFactoryTest implements NamedValueTester<BuildType> {
		@Override
		public BuildType createSubject(String name) {
			return named(name);
		}
	}

	@Test
	void hasDefaultBuildTypeName() {
		assertThat(BuildType.DEFAULT, equalTo("default"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEqualityAgainstObjectFactoryNamedInstance() {
		new EqualsTester()
			.addEqualityGroup((Object[]) equalityGroupFor(named("debug")))
			.addEqualityGroup((Object[]) equalityGroupFor(named("release")))
			.addEqualityGroup((Object[]) equalityGroupFor(named("RelWithDebug")))
			.testEquals();
	}

	private static BuildType[] equalityGroupFor(BuildType v) {
		return new BuildType[] {
			objectFactory().named(BuildType.class, v.getName()),
			v,
			objectFactory().named(BuildType.class, v.getName())
		};
	}
}
