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
package dev.nokee.buildadapter.xcode;

import dev.gradleplugins.buildscript.blocks.ProjectBlock;
import dev.gradleplugins.buildscript.blocks.TaskBlock;
import dev.gradleplugins.buildscript.blocks.TasksBlock;
import dev.gradleplugins.buildscript.statements.GroupStatement;
import dev.gradleplugins.buildscript.statements.Statement;
import org.gradle.api.Action;

import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.statements.Statement.expressionOf;
import static dev.gradleplugins.buildscript.syntax.Syntax.invoke;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;

public final class GradleTestSnippets {
	private GradleTestSnippets() {}

	public static Statement doSomethingVerifyTask() {
		return TasksBlock.tasks(it -> it.register("verify", TaskBlock.Builder::doLast));
	}

	/**
	 * Creates a task named {@literal verify} where the {@literal doLast} action executes the specified assertions.
	 *
	 * Note the assertions are assumed to be DSL agnostic.
	 *
	 * @param assertions  DSL agnostic boolean assertion, must not be null
	 * @return a statement for the verify task registration, never null
	 */
	public static Statement assertVerifyTask(String... assertions) {
		return TasksBlock.tasks(it -> it.register("verify", task -> task.doLast(builder -> {
			for (String assertion : assertions) {
				builder.add(expressionOf(invoke("assert", literal(assertion))));
			}
		})));
	}

	public static Consumer<ProjectBlock.Builder> registerVerifyTask(Consumer<? super GroupStatement.Builder> builderConsumer) {
		return project -> project.add(TasksBlock.tasks(tasks -> tasks.register("verify", task -> task.doLast(builderConsumer))));
	}

	public static Consumer<GroupStatement.Builder> assertStatements(String... assertions) {
		return builder -> {
			for (String assertion : assertions) {
				builder.add(expressionOf(invoke("assert", literal(assertion))));
			}
		};
	}
}
