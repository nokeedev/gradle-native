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
package dev.nokee.nvm.fixtures;

import java.nio.file.Path;

public final class TestLayout extends AbstractTestGradleBuild<TestLayout> implements TestGradleBuild {
	private TestLayout(Path location) {
		super(location);
	}

	public static TestLayout newBuild(Path location) {
		return new TestLayout(location);
	}

	// TODO: Return BuildScriptFile via getBuildFile()

	// TODO: Return BuildScriptFile via getSettingsFile()

//	interface SubprojectBuilder {
//		// build filename -> build
//		SubprojectBuilder baseName(String baseName);
//
//		// DSL => Groovy DSL (or general default)
//		SubprojectBuilder dsl(GradleDsl dsl);
//
//		// Gradle path
//		SubprojectBuilder path(String path);
//
//		SubprojectBuilder buildFile(Consumer<? super ProjectBlock.Builder> action);
//	}
//
}
