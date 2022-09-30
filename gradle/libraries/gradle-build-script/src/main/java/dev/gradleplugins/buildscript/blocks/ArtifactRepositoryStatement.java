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
import dev.gradleplugins.buildscript.statements.ExpressionStatement;
import dev.gradleplugins.buildscript.statements.GroupStatement;
import dev.gradleplugins.buildscript.statements.Statement;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.function.Consumer;

import static dev.gradleplugins.buildscript.syntax.Syntax.invoke;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.property;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;

public final class ArtifactRepositoryStatement extends AbstractBlock {
	public static Statement gradlePluginPortal() {
		return ExpressionStatement.of(invoke("gradlePluginPortal"));
	}

	public static Statement mavenLocal() {
		return ExpressionStatement.of(invoke("mavenLocal"));
	}

	public static Statement mavenCentral() {
		return ExpressionStatement.of(invoke("mavenCentral"));
	}

	public static BlockStatement<ArtifactRepositoryStatement> maven(URI uri) {
		return BlockStatement.of(literal("maven"), new Builder().url(uri).build());
	}

	public static BlockStatement<ArtifactRepositoryStatement> maven(Consumer<? super Builder> action) {
		final Builder builder = new Builder();
		action.accept(builder);
		return BlockStatement.of(literal("maven"), builder.build());
	}

	public static BlockStatement<ArtifactRepositoryStatement> ivy(URI uri) {
		return BlockStatement.of(literal("ivy"), new Builder().url(uri).build());
	}

	public static BlockStatement<ArtifactRepositoryStatement> ivy(Consumer<? super Builder> action) {
		final Builder builder = new Builder();
		action.accept(builder);
		return BlockStatement.of(literal("ivy"), builder.build());
	}

	private ArtifactRepositoryStatement(Statement delegate) {
		super(delegate);
	}

	public static final class Builder {
		private String name;
		private URI uri;

		public Builder name(String name) {
			this.name = Objects.requireNonNull(name);
			return this;
		}

		public Builder url(URI uri) {
			this.uri = Objects.requireNonNull(uri);
			return this;
		}

		public ArtifactRepositoryStatement build() {
			Objects.requireNonNull(uri);
			final GroupStatement.Builder builder = GroupStatement.builder();
			if (name != null) {
				builder.add(ExpressionStatement.of(property(literal("name")).assign(string(name))));
			}
			builder.add(ExpressionStatement.of(property(literal("url")).assign(invoke("uri", string(uri.toString())).alwaysUseParenthesis())));
			return new ArtifactRepositoryStatement(builder.build());
		}
	}

	public static URI uri(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
