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
	@SuppressWarnings("UnstableApiUsage")
	void checkEqualityAgainstObjectFactoryNamedInstance() {
		new EqualsTester()
			.addEqualityGroup((Object[]) equalityGroupFor("debug"))
			.addEqualityGroup((Object[]) equalityGroupFor("release"))
			.addEqualityGroup((Object[]) equalityGroupFor("RelWithDebug"))
			.testEquals();
	}

	private static BuildType[] equalityGroupFor(String name) {
		return new BuildType[] {
			objectFactory().named(BuildType.class, name),
			named(name),
			objectFactory().named(BuildType.class, name)
		};
	}
}
