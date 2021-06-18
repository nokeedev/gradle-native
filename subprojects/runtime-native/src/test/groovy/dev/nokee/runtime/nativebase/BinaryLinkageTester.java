package dev.nokee.runtime.nativebase;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

public interface BinaryLinkageTester extends NamedValueTester<BinaryLinkage> {
	@Test
	default void canCreateSharedBinaryLinkage() {
		val subject = createSubject("shared");
		assertAll(
			() -> assertThat(subject.isShared(), is(true)),
			() -> assertThat(subject.isStatic(), is(false)),
			() -> assertThat(subject.isBundle(), is(false)),
			() -> assertThat(subject.isExecutable(), is(false))
		);
	}

	@Test
	default void canCreateStaticBinaryLinkage() {
		val subject = createSubject("static");
		assertAll(
			() -> assertThat(subject.isShared(), is(false)),
			() -> assertThat(subject.isStatic(), is(true)),
			() -> assertThat(subject.isBundle(), is(false)),
			() -> assertThat(subject.isExecutable(), is(false))
		);
	}

	@Test
	default void canCreateBundleBinaryLinkage() {
		val subject = createSubject("bundle");
		assertAll(
			() -> assertThat(subject.isShared(), is(false)),
			() -> assertThat(subject.isStatic(), is(false)),
			() -> assertThat(subject.isBundle(), is(true)),
			() -> assertThat(subject.isExecutable(), is(false))
		);
	}

	@Test
	default void canCreateExecutableBinaryLinkage() {
		val subject = createSubject("executable");
		assertAll(
			() -> assertThat(subject.isShared(), is(false)),
			() -> assertThat(subject.isStatic(), is(false)),
			() -> assertThat(subject.isBundle(), is(false)),
			() -> assertThat(subject.isExecutable(), is(true))
		);
	}
}
