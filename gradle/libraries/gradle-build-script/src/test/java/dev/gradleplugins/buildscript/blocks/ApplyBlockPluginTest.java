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

import dev.gradleplugins.buildscript.TestPlugin;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.BuildScriptMatchers.groovyOf;
import static dev.gradleplugins.buildscript.blocks.ApplyStatement.Notation.plugin;
import static dev.gradleplugins.buildscript.blocks.ApplyStatement.apply;
import static org.hamcrest.MatcherAssert.assertThat;

class ApplyBlockPluginTest {
	@Test
	void rendersApplyByPluginIdInGroovy() {
		assertThat(apply(plugin("com.example.foo")), groovyOf("apply plugin: 'com.example.foo'"));
	}

	@Test
	void rendersApplyByPluginClassInGroovy() {
		assertThat(apply(ApplyStatement.Notation.plugin(TestPlugin.class)), groovyOf("apply plugin: " + TestPlugin.class.getCanonicalName()));
	}
}
