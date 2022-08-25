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
package dev.nokee.nvm;

import dev.gradleplugins.buildscript.blocks.TaskBlock;
import dev.gradleplugins.buildscript.statements.Statement;
import dev.gradleplugins.buildscript.syntax.Syntax;
import dev.gradleplugins.testscript.HasFileSystem;
import dev.gradleplugins.testscript.TestBuildSrc;
import dev.gradleplugins.testscript.TestGradleBuild;
import dev.gradleplugins.testscript.TestIncludedBuild;

import java.util.function.Consumer;

public final class ProjectFixtures {
	public static <T extends TestGradleBuild> Consumer<T> applyPluginUnderTest() {
		return build -> {
			build.settingsFile(settings -> settings.plugins(it -> it.id("dev.nokee.nokee-version-management")));
		};
	}

	public static <T extends TestGradleBuild> Consumer<T> registerVerifyTask() {
		return registerVerifyTask(__ -> {});
	}

	public static <T extends TestGradleBuild> Consumer<T> registerVerifyTask(Consumer<? super TaskBlock.Builder> action) {
		return it -> {
			if (it instanceof TestBuildSrc) {
				it.buildFile(project -> project.tasks(tasks -> {
					tasks.register("verify", action);
					tasks.named("build", task -> task.finalizedBy("verify"));
				}));
			} else if (it instanceof TestIncludedBuild) {
				((TestIncludedBuild) it).parentBuild(parent -> {
					parent.buildFile(project -> project.tasks(tasks -> tasks.named("verify", task -> {
						task.dependsOn("gradle.includedBuild(\"" + ((TestIncludedBuild) it).buildPath() + "\").task(\":verify\")");
					})));
				});
				it.buildFile(project -> project.tasks(tasks -> tasks.register("verify", action)));
			} else {
				it.buildFile(project -> project.tasks(tasks -> tasks.register("verify", action)));
			}
		};
	}

	public static <T extends TestGradleBuild> Consumer<T> configureVerifyTask(Consumer<? super TaskBlock.Builder> action) {
		return it -> {
			it.buildFile(project -> project.tasks(tasks -> {
				tasks.named("verify", action);
			}));
		};
	}

	private static char nextChildName = 'A';

	public static BuildConsumer nokeeBuild() {
		return nokeeBuild(__ -> {});
	}

	public static BuildConsumer nokeeBuild(Consumer<? super TestGradleBuild> consumer) {
		return new BuildConsumer() {
			@Override
			public void accept(TestGradleBuild build) {
				registerVerifyTask().accept(build);
				applyPluginUnderTest().accept(build);
				consumer.accept(build);
			}
		};
	}

	public static Consumer<TestGradleBuild> applyAnyNokeePlugin() {
		return build -> {
			build.buildFile(project -> project.plugins(it -> it.id("dev.nokee.jni-library")));
		};
	}

	public static Consumer<TestGradleBuild> withVersion(String version) {
		return writeVersionFile(version);
	}

	public static BuildConsumer nonNokeeBuild() {
		return new BuildConsumer() {
			@Override
			public void accept(TestGradleBuild build) {
				registerVerifyTask().accept(build);
			}
		};
	}

	public static BuildConsumer nonNokeeBuild(Consumer<? super TestGradleBuild> consumer) {
		return new BuildConsumer() {
			@Override
			public void accept(TestGradleBuild build) {
				registerVerifyTask().accept(build);
				consumer.accept(build);
			}
		};
	}

	public interface BuildConsumer extends Consumer<TestGradleBuild> {
		default BuildConsumer parentOf(Consumer<TestGradleBuild> childConsumer) {
			return new BuildConsumer() {
				@Override
				public void accept(TestGradleBuild build) {
					BuildConsumer.this.accept(build);
					build.includeBuild(String.valueOf(nextChildName++), childConsumer);
				}
			};
		}

		default BuildConsumer parentOf(String childName, Consumer<TestGradleBuild> childConsumer) {
			return new BuildConsumer() {
				@Override
				public void accept(TestGradleBuild build) {
					BuildConsumer.this.accept(build);
					build.includeBuild(childName, childConsumer);
				}
			};
		}
	}

	public static Consumer<TestGradleBuild> expect(String version) {
		return configureVerifyTask(usesServiceUnderTest().andThen(assertNokeeVersion(version)));
	}

	public static Consumer<TestGradleBuild> resolveVersion() {
		return configureVerifyTask(usesServiceUnderTest().andThen(resolveNokeeVersion()));
	}

	private static Consumer<TaskBlock.Builder> resolveNokeeVersion() {
		return builder -> {
			builder.doLast(task -> task.add(Statement.expressionOf(Syntax.groovy("dev.nokee.nvm.NokeeVersionManagementService.fromBuild(gradle).get().version.toString()"))));
		};
	}

	public static <T extends HasFileSystem> Consumer<T> writeVersionFile(String version) {
		return it -> it.file(".nokee-version", version);
	}

	public static Consumer<TaskBlock.Builder> usesServiceUnderTest() {
		return builder -> {
			builder.usesService("nokeeVersionManagement");
		};
	}

	public static Consumer<TaskBlock.Builder> assertNokeeVersion(String version) {
		return builder -> {
			builder.doLast(task -> task.add(Statement.expressionOf(Syntax.gradle(
				"assert dev.nokee.nvm.NokeeVersionManagementService.fromBuild(gradle).get().version.toString() == '" + version + "'",
				"assert(dev.nokee.nvm.NokeeVersionManagementService.fromBuild(gradle).get().version.toString() == \"" + version + "\")"
				))));
		};
	}
}
