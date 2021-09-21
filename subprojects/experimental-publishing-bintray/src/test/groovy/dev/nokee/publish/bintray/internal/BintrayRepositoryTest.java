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
package dev.nokee.publish.bintray.internal;

import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.net.URI;
import java.net.URISyntaxException;

import static dev.nokee.publish.bintray.internal.BintrayCredentials.of;
import static dev.nokee.publish.bintray.internal.BintrayPackageName.of;
import static dev.nokee.publish.bintray.internal.BintrayTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Subject(BintrayRepository.class)
class BintrayRepositoryTest {
	@Test
	void canCreateBintrayRepositoryFromRepositoryDeclaration() throws URISyntaxException {
		val repository = new BintrayRepository(bintrayUrl("foo/examples"), of("eve"), of("bob", "alice"));
		assertThat(repository.getUrl(), equalTo(new URI("https://dl.bintray.com/foo/examples")));
		assertThat(repository.getPackageName(), equalTo(of("eve")));
		assertThat(repository.getCredentials().getBintrayUser(), equalTo("bob"));
		assertThat(repository.getCredentials().getBintrayKey(), equalTo("alice"));
	}
}
