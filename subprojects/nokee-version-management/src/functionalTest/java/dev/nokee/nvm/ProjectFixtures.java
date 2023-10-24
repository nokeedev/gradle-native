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

import dev.gradleplugins.buildscript.ast.statements.GradleBlockStatement;
import dev.gradleplugins.testscript.HasFileSystem;
import dev.gradleplugins.testscript.TestBuildSrc;
import dev.gradleplugins.testscript.TestGradleBuild;
import dev.gradleplugins.testscript.TestIncludedBuild;

import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.ast.expressions.MethodCallExpression.call;
import static dev.gradleplugins.buildscript.ast.statements.GradleBlockStatement.block;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;

public final class ProjectFixtures {
	public static <T extends TestGradleBuild> Consumer<T> applyPluginUnderTest() {
		return build -> {
			build.getSettingsFile().plugins(it -> it.id("dev.nokee.nokee-version-management"));
		};
	}

	public static <T extends TestGradleBuild> Consumer<T> registerVerifyTask() {
		return registerVerifyTask(__ -> {});
	}

	public static <T extends TestGradleBuild> Consumer<T> registerVerifyTask(Consumer<? super GradleBlockStatement.BlockBuilder<?>> action) {
		return it -> {
			if (it instanceof TestBuildSrc) {
				it.buildFile(project -> project.tasks(tasks -> {
					tasks.add(block(tasks.delegate().call("register", string("verify")), action));
					tasks.add(block(tasks.delegate().call("named", string("build")),
						(GradleBlockStatement.BlockBuilder<?> task) -> task.add(task.delegate().call("finalizedBy", string("verify")))));
				}));
			} else if (it instanceof TestIncludedBuild) {
				((TestIncludedBuild) it).parentBuild(parent -> {
					parent.buildFile(project -> project.tasks(tasks -> {
						tasks.add(block(tasks.delegate().call("named", string("verify")), task -> {
							task.add(groovyDsl("dependsOn(gradle.includedBuild(\"" + ((TestIncludedBuild) it).buildPath() + "\").task(\":verify\"))"));
						}));
					}));
				});
				it.buildFile(project -> project.tasks(tasks ->
					tasks.add(block(tasks.delegate().call("register", string("verify")), action))));
			} else {
				it.buildFile(project -> project.tasks(tasks ->
					tasks.add(block(tasks.delegate().call("register", string("verify")), action))));
			}
		};
	}

	public static <T extends TestGradleBuild> Consumer<T> configureVerifyTask(Consumer<? super GradleBlockStatement.BlockBuilder<?>> action) {
		return it -> {
			it.buildFile(project -> project.tasks(tasks -> {
				tasks.add(block(tasks.delegate().call("named", string("verify")), action));
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
			build.getBuildFile().plugins(it -> it.id("dev.nokee.jni-library"));
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

	private static Consumer<GradleBlockStatement.BlockBuilder<?>> resolveNokeeVersion() {
		return builder -> {
			builder.add(block("doLast", task -> task.add(groovyDsl("dev.nokee.nvm.NokeeVersionManagementService.fromBuild(gradle).get().version.toString()"))));
		};
	}

	public static <T extends HasFileSystem> Consumer<T> writeVersionFile(String version) {
		return it -> it.file(".nokee-version", version);
	}

	public static Consumer<GradleBlockStatement.BlockBuilder<?>> usesServiceUnderTest() {
		return builder -> {
			builder.add(call("usesService", groovyDsl("gradle.sharedServices.registrations.getByName(\"nokeeVersionManagement\").service")));
		};
	}

	public static Consumer<GradleBlockStatement.BlockBuilder<?>> assertNokeeVersion(String version) {
		return builder -> {
			builder.add(block("doLast", task -> task.add(groovyDsl(
				"assert dev.nokee.nvm.NokeeVersionManagementService.fromBuild(gradle).get().version.toString() == '" + version + "'"
				))));
		};
	}
}
