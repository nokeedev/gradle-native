package dev.nokee.runtime.nativebase;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.runtime.nativebase.BinaryLinkage.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class BinaryLinkageTest {
	@Nested
	class ObjectFactoryTest implements BinaryLinkageTester {
		@Override
		public BinaryLinkage createSubject(String name) {
			return objectFactory().named(BinaryLinkage.class, name);
		}
	}

	@Nested
	class MethodFactoryTest implements BinaryLinkageTester {
		@Override
		public BinaryLinkage createSubject(String name) {
			return named(name);
		}
	}

	@Test
	void hasSharedLinkageName() {
		assertThat(BinaryLinkage.SHARED, equalTo("shared"));
	}

	@Test
	void hasStaticLinkageName() {
		assertThat(BinaryLinkage.STATIC, equalTo("static"));
	}

	@Test
	void hasBundleLinkageName() {
		assertThat(BinaryLinkage.BUNDLE, equalTo("bundle"));
	}

	@Test
	void hasExecutableLinkageName() {
		assertThat(BinaryLinkage.EXECUTABLE, equalTo("executable"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEqualityAgainstObjectFactoryNamedInstance() {
		new EqualsTester()
			.addEqualityGroup((Object[]) equalityGroupFor(BinaryLinkage.STATIC))
			.addEqualityGroup((Object[]) equalityGroupFor(BinaryLinkage.SHARED))
			.addEqualityGroup((Object[]) equalityGroupFor(BinaryLinkage.BUNDLE))
			.addEqualityGroup((Object[]) equalityGroupFor(BinaryLinkage.EXECUTABLE))
			.testEquals();
	}

	private static BinaryLinkage[] equalityGroupFor(String name) {
		return new BinaryLinkage[] {
			objectFactory().named(BinaryLinkage.class, name),
			named(name),
			objectFactory().named(BinaryLinkage.class, name)
		};
	}
}
