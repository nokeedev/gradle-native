package dev.nokee.publish.bintray.internal;

import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.publish.bintray.internal.BintrayPackageName.fromRepositoryDeclaration;
import static dev.nokee.publish.bintray.internal.BintrayPackageName.of;
import static dev.nokee.publish.bintray.internal.BintrayTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(BintrayPackageName.class)
class BintrayPackageNameTest {
	@Test
	void doesNotThrowExceptionWhenCreatingBintrayPackageNameFromRepositoryWithMissingExtensionProperty() {
		assertDoesNotThrow(() -> fromRepositoryDeclaration(repository(withoutPackageName())));
	}

	@Test
	void throwsExceptionWhenQueryingBintrayPackageNameFromRepositoryWithMissingExtensionProperty() {
		val ex = assertThrows(IllegalStateException.class, () -> fromRepositoryDeclaration(repository(withoutPackageName())).get());
		assertThat(ex.getMessage(), equalTo("When publishing to Bintray repositories, please specify the package name using an extra property on the repository, e.g. ext.packageName = 'foo'."));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(BintrayPackageName.class);
	}

	@Test
	void canQueryBintrayPackageNameFromRepositoryWithExtensionProperty() {
		assertThat(fromRepositoryDeclaration(repository(withPackageName("foo"))).get(), equalTo("foo"));
	}

	@Test
	void canCreateBintrayPackageNameFromStringInstance() {
		assertThat(of("foo").get(), equalTo("foo"));
	}
}
