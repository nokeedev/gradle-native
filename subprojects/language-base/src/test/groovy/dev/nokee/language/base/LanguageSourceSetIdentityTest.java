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
package dev.nokee.language.base;

import com.google.common.testing.EqualsTester;
import dev.nokee.language.base.internal.LanguageSourceSetIdentity;
import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.model.testers.HasNameTester;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.nokee.language.base.internal.LanguageSourceSetIdentity.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageSourceSetIdentityTest implements HasNameTester {
	private final LanguageSourceSetIdentity subject = of("gofe", "zewi duna");

	@Override
	public LanguageSourceSetIdentity subject() {
		return subject;
	}

	@Test
	public void hasName() {
		assertEquals(LanguageSourceSetName.of("gofe"), subject().getName());
	}

	@Test
	void hasDisplayName()  {
		assertEquals("zewi duna", subject.getDisplayName());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(of("kofo", "sources"), of("kofo", "sources"))
			.addEqualityGroup(of("tiri", "sources"))
			.addEqualityGroup(of("kofo", "other sources"))
			.testEquals();
	}

	@Test
	void hasToString() {
		assertThat(subject, Matchers.hasToString("sources 'gofe'"));
	}
}
