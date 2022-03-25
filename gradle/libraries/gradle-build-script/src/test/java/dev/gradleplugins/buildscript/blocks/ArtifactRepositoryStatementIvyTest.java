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

import static dev.gradleplugins.buildscript.BuildScriptMatchers.visit;
import static dev.gradleplugins.buildscript.blocks.ArtifactRepositoryStatement.ivy;
import static dev.gradleplugins.buildscript.blocks.ArtifactRepositoryStatement.uri;
import static org.hamcrest.MatcherAssert.assertThat;

class ArtifactRepositoryStatementIvyTest {
	@Test
	void rendersIvyWithUrlOnly() {
		assertThat(ivy(uri("https://repo.example.com/release")), visit(
			"<block selector='ivy'>",
			"<statement>url = uri('https://repo.example.com/release')</statement>",
			"</block>"
		));
	}

	@Test
	void rendersConfiguredIvyWithUrlOnly() {
		assertThat(ivy(t -> t.url(uri("https://repo.example.com/snapshot"))), visit(
			"<block selector='ivy'>",
			"<statement>url = uri('https://repo.example.com/snapshot')</statement>",
			"</block>"
		));
	}
}
