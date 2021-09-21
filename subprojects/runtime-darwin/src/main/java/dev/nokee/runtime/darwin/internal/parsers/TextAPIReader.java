/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.runtime.darwin.internal.parsers;

import dev.nokee.runtime.darwin.internal.XcodeSdk;
import lombok.val;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;

public final class TextAPIReader implements Closeable {
	private final Reader reader;

	public TextAPIReader(Reader reader) {
		this.reader = reader;
	}

	public TextAPI read() throws IOException {
		dev.nokee.runtime.darwin.internal.parsers.TextAPILexer lexer = new dev.nokee.runtime.darwin.internal.parsers.TextAPILexer(CharStreams.fromReader(reader));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		dev.nokee.runtime.darwin.internal.parsers.TextAPIParser parser = new dev.nokee.runtime.darwin.internal.parsers.TextAPIParser(tokens);

		val l = new ExtractTextAPI();
		ParseTreeWalker.DEFAULT.walk((ParseTreeListener)l, parser.tbd());
		return l.builder.build();
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	private static class ExtractTextAPI extends dev.nokee.runtime.darwin.internal.parsers.TextAPIBaseListener {
		private final TextAPI.Builder builder = TextAPI.builder();

		@Override
		public void enterTargets(dev.nokee.runtime.darwin.internal.parsers.TextAPIParser.TargetsContext ctx) {
			super.enterTargets(ctx);
			for (dev.nokee.runtime.darwin.internal.parsers.TextAPIParser.ValueContext valueContext : ctx.array().value()) {
				val tokens = valueContext.getText().split("-");
				builder.target(new TextAPI.Target(tokens[0], tokens[1]));
			}
		}
	}
}
