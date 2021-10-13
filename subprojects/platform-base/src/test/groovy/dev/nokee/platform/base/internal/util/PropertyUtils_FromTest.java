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
package dev.nokee.platform.base.internal.util;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableInt;
import org.gradle.api.file.ConfigurableFileCollection;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.function.Function;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyUtils_FromTest {
	private static final Object SELF = new Object();
	private final ConfigurableFileCollection target = objectFactory().fileCollection();
	private final PropertyUtils.FileCollectionProperty subject = PropertyUtils.wrap(target);
	private final File someValue = assertDoesNotThrow(() -> File.createTempFile("dar", ".txt"));
	private final File someOtherValue = assertDoesNotThrow(() -> File.createTempFile("other-dar", ".txt"));

	private Iterable<File> subjectValue() {
		return target.getFiles();
	}

	@Test
	void canAddMultipleValues() {
		from(someValue, someOtherValue).accept(SELF, subject);
		assertThat(subjectValue(), contains(someValue, someOtherValue));
	}

	@Test
	void canAddMultipleValuesAsIterable() {
		from(ImmutableList.of(someValue, someOtherValue)).accept(SELF, subject);
		assertThat(subjectValue(), contains(someValue, someOtherValue));
	}

	@Test
	void canMapValueToAddFromOwner() {
		from(it -> {
			assertThat(it, is(SELF));
			return ImmutableList.of(someOtherValue, someValue);
		}).accept(SELF, subject);
		assertThat(subjectValue(), contains(someOtherValue, someValue));
	}

	@Test
	void callingTheActionMultipleTimeMapTheOwnerMultipleTime() {
		val callCount = new MutableInt(0);
		val action = from(it -> {
			assertThat(it, is(SELF));
			callCount.increment();
			return ImmutableList.of(someValue);
		});
		action.accept(SELF, subject);
		action.accept(SELF, subject);
		assertThat(callCount.intValue(), is(2));
	}

	@Test
	void throwsExceptionIfMapperFunctionIsNull() {
		assertThrows(NullPointerException.class, () -> from((Function<? super Object, Iterable<?>>) null));
	}
}
