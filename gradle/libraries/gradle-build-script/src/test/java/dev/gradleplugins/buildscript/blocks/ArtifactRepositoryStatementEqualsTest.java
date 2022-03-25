/*
 * Copyright 2022 the original author or authors.
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
package dev.gradleplugins.buildscript.blocks;

import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.blocks.ArtifactRepositoryStatement.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

class ArtifactRepositoryStatementEqualsTest {
	@Test
	void defaultMavenCentralIsEqualsBetweenEachOther() {
		assertThat(mavenCentral(), equalTo(mavenCentral()));
	}

	@Test
	void defaultMavenLocalIsEqualBetweenEachOther() {
		assertThat(mavenLocal(), equalTo(mavenLocal()));
	}

	@Test
	void defaultMavenLocalIsNotEqualToMavenWithUrlOnly() {
		assertThat(mavenLocal(), not(equalTo(maven(uri("https://repo.example.com/snapshot")))));
	}

	@Test
	void defaultMavenLocalIsNotEqualToMavenConfiguredWithUrlOnly() {
		assertThat(mavenLocal(), not(equalTo(maven(t -> t.url(uri("https://repo.example.com/snapshot"))))));
	}

	@Test
	void defaultMavenLocalIsNotEqualToDefaultMavenCentral() {
		assertThat(mavenLocal(), not(equalTo(mavenCentral())));
	}

	@Test
	void mavenWithUrlOnlyIsEqualToMavenConfiguredWithUrlOnly() {
		assertThat(maven(uri("https://repo.example.com/snapshot")),
			equalTo(maven(t -> t.url(uri("https://repo.example.com/snapshot")))));
	}

	@Test
	void mavenWithUrlOnlyIsNotEqualToMavenConfiguredWithUrlOnlyUsingDifferentUrls() {
		assertThat(maven(uri("https://repo.example.com/release")),
			not(equalTo(maven(t -> t.url(uri("https://repo.example.com/snapshot"))))));
	}

	@Test
	void ivyWithUrlOnlyIsEqualToIvyConfiguredWithUrlOnly() {
		assertThat(ivy(uri("https://repo.example.com/snapshot")),
			equalTo(ivy(t -> t.url(uri("https://repo.example.com/snapshot")))));
	}

	@Test
	void ivyWithUrlOnlyIsNotEqualToIvyConfiguredWithUrlOnlyUsingDifferentUrls() {
		assertThat(ivy(uri("https://repo.example.com/release")),
			not(equalTo(ivy(t -> t.url(uri("https://repo.example.com/snapshot"))))));
	}

	@Test
	void ivyBlockNotEqualToMavenBlockWithSameUrl() {
		assertThat(ivy(uri("https://repo.example.com/release")),
			not(equalTo(maven(uri("https://repo.example.com/release")))));
	}
}
