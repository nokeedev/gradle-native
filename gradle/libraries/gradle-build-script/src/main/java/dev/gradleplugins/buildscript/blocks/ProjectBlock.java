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
import dev.gradleplugins.buildscript.syntax.Expression;
import lombok.val;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.gradleplugins.buildscript.statements.GroupStatement.toGroupStatement;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.property;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;
import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.toList;

public final class ProjectBlock extends AbstractBlock {
	private ProjectBlock(Statement delegate) {
		super(delegate);
	}

	public static BlockStatement<ProjectBlock> allprojects(Consumer<? super ProjectBlock.Builder> action) {
		final Builder builder = new Builder();
		action.accept(builder);
		return BlockStatement.of(literal("allprojects"), builder.build());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final GroupStatement.Builder builder = GroupStatement.builder();
		private final Set<String> alreadyCreatedConfigurations = new HashSet<>();

		public Builder add(Statement statement) {
			builder.add(GroupStatement.of(statement));
			return this;
		}

		public Builder block(String selector, Consumer<? super GroupStatement.Builder> action) {
			final GroupStatement.Builder builder = GroupStatement.builder();
			action.accept(builder);
			this.builder.add(GroupStatement.of(BlockStatement.of(literal(selector), builder.build())));
			return this;
		}

		public Builder repositories(Consumer<? super RepositoriesBlock.Builder> action) {
			builder.add(GroupStatement.of(RepositoriesBlock.repositories(action)));
			return this;
		}

		public Builder dependencies(Consumer<? super DependenciesBlock.Builder> action) {
			builder.add(GroupStatement.of(DependenciesBlock.dependencies(action)));
			return this;
		}

		public Builder configurations(Consumer<? super ConfigurationsBlock.Builder> action) {
			builder.add(GroupStatement.of(ConfigurationsBlock.configurations(alreadyCreatedConfigurations, action)));
			return this;
		}

		// FIXME: Only in actual project file...
		public Builder buildscript(Consumer<? super BuildScriptBlock.Builder> action) {
			builder.add(GroupStatement.of(BuildScriptBlock.buildscript(action)));
			return this;
		}

		// FIXME: Only in actual proejct file...
		public Builder plugins(Consumer<? super PluginsBlock.Builder> action) {
			builder.add(GroupStatement.of(PluginsBlock.plugins(action)));
			return this;
		}

		public Builder allprojects(Consumer<? super ProjectBlock.Builder> action) {
			builder.add(GroupStatement.of(ProjectBlock.allprojects(action)));
			return this;
		}

		public Builder apply(ApplyStatement.Notation notation) {
			builder.add(ApplyStatement.apply(notation));
			return this;
		}

		public Builder setGroup(String group) {
			builder.add(Statement.expressionOf(property(literal("group")).assign(string(group))));
			return this;
		}

		public Builder tasks(Consumer<? super TasksBlock.Builder> action) {
			builder.add(TasksBlock.tasks(action));
			return this;
		}

		public ProjectBlock build() {
			return new ProjectBlock(normalize(builder.build()));
		}

		// TODO: Handle comment before block -> comment before group inside block
		private static GroupStatement normalize(Iterable<Statement> statements) {
			return StreamSupport.stream(statements.spliterator(), false)
				.collect(Collectors.groupingBy(BlockType::classify, LinkedHashMap::new, toList()))
				.entrySet().stream().sorted(comparingByKey()).flatMap(it -> {
					if (it.getKey().equals(BlockType.OTHERS)) {
						return it.getValue().stream();
					} else {
						val builder = GroupStatement.builder();
						it.getValue().stream()
							.map(ProjectBlock.Builder::unpackToBlockStatement)
							.map(BlockStatement::getContent)
							.forEach(builder::add);
						return Stream.of(GroupStatement.of(BlockStatement.of(it.getKey().blockSelector(), builder.build())));
					}
				}).collect(toGroupStatement());
		}

		private static BlockStatement<?> unpackToBlockStatement(Statement statement) {
			if (statement instanceof BlockStatement) {
				return (BlockStatement<?>) statement;
			} else if (statement instanceof GroupStatement) {
				assert ((GroupStatement) statement).size() == 1;
				return unpackToBlockStatement(((GroupStatement) statement).iterator().next());
			} else {
				throw new UnsupportedOperationException();
			}
		}

		private enum BlockType {
			BUILDSCRIPT("buildscript"),
			PLUGINS("plugins"),
			OTHERS(null);

			private final String selector;

			BlockType(String selector) {
				this.selector = selector;
			}

			public Expression blockSelector() {
				return literal(selector);
			}

			public static BlockType classify(Statement statement) {
				Statement nestedStatement = statement;
				if (statement instanceof GroupStatement && ((GroupStatement) statement).size() == 1) {
					nestedStatement = ((GroupStatement) statement).iterator().next();
				}

				if (nestedStatement instanceof BlockStatement) {
					Statement nestedBlockContentStatement = ((BlockStatement<?>) nestedStatement).getContent();
					if (nestedBlockContentStatement instanceof PluginsBlock) {
						return PLUGINS;
					} else if (nestedBlockContentStatement instanceof BuildScriptBlock) {
						return BUILDSCRIPT;
					}
				}
				return OTHERS;
			}
		}
	}
}
