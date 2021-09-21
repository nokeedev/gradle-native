/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
