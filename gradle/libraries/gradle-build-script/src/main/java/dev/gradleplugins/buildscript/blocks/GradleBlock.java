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

import dev.gradleplugins.buildscript.statements.BlockStatement;
import dev.gradleplugins.buildscript.statements.GroupStatement;
import dev.gradleplugins.buildscript.statements.Statement;

import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.syntax.Syntax.literal;

public final class GradleBlock extends AbstractBlock {

	private GradleBlock(Statement delegate) {
		super(delegate);
	}

	public static BlockStatement<ProjectBlock> rootProject(Consumer<? super ProjectBlock.Builder> action) {
		final ProjectBlock.Builder builder = ProjectBlock.builder();
		action.accept(builder);
		return BlockStatement.of(literal("rootProject"), builder.build());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final GroupStatement.Builder builder = GroupStatement.builder();

		public Builder initscript(Consumer<? super BuildScriptBlock.Builder> action) {
			builder.add(BuildScriptBlock.initscript(action));
			return this;
		}

		public Builder settingsEvaluated(Consumer<? super SettingsBlock.Builder> action) {
			final SettingsBlock.Builder builder = new SettingsBlock.Builder();
			action.accept(builder);
			this.builder.add(BlockStatement.of(literal("settingsEvaluated"), builder.build()).useExplicitIt());
			return this;
		}

		public Builder beforeSettings(Consumer<? super SettingsBlock.Builder> action) {
			final SettingsBlock.Builder builder = new SettingsBlock.Builder();
			action.accept(builder);
			this.builder.add(BlockStatement.of(literal("beforeSettings"), builder.build()).useExplicitIt());
			return this;
		}

		public GradleBlock build() {
			return new GradleBlock(builder.build());
		}
	}
}
