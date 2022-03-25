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
import static dev.gradleplugins.buildscript.statements.Statement.empty;
import static dev.gradleplugins.buildscript.blocks.RepositoriesBlock.repositories;
import static org.hamcrest.MatcherAssert.assertThat;

class RepositoriesBlockRenderTest {
	@Test
	void canRenderEmptyRepositoriesBlock() {
		assertThat(repositories(empty()), visit("<block selector='repositories'/>"));
	}

	@Test
	void rendersRepositoriesBlock() {
		assertThat(repositories(t -> t.mavenLocal().mavenCentral()), visit(
			"<block selector='repositories'><group>",
			"<statement>mavenLocal()</statement>",
			"<statement>mavenCentral()</statement>",
			"</group></block>"
		));
	}

	@Test
	void rendersRepositoriesBlockWithCommentOnFirstStatement() {
		assertThat(repositories(t -> t.comment("Plugin repository", it -> it.gradlePluginPortal())), visit(
			"<block selector='repositories'>",
			"<!-- Plugin repository -->",
			"<statement>gradlePluginPortal()</statement>",
			"</block>"
		));
	}

	@Test
	void rendersRepositoriesBlockWithCommentOnOtherStatement() {
		assertThat(repositories(t -> t.mavenLocal().comment("Plugin repository", it -> it.gradlePluginPortal())), visit(
			"<block selector='repositories'><group>",
			"<statement>mavenLocal()</statement>",
			"<!-- Plugin repository -->",
			"<statement>gradlePluginPortal()</statement>",
			"</group></block>"
		));
	}

	@Test
	void rendersRepositoriesBlockWithUrlOnlyMaven() {
		assertThat(repositories(t -> t.maven("https://repo.nokee.dev/release")), visit(
			"<block selector='repositories'>",
			"<block selector='maven'>",
			"<statement>url = uri('https://repo.nokee.dev/release')</statement>",
			"</block>",
			"</block>"
		));
	}
}
