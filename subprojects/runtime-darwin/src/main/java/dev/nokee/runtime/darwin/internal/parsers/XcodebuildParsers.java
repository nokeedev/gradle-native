/*
 * Copyright 2020 the original author or authors.
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

import dev.nokee.core.exec.CommandLineToolOutputParser;
import dev.nokee.runtime.darwin.internal.XcodeSdk;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.LinkedHashSet;
import java.util.Set;

public class XcodebuildParsers {
	public static CommandLineToolOutputParser<Set<XcodeSdk>> showSdkParser() {
		return new CommandLineToolOutputParser<Set<XcodeSdk>>() {

			@Override
			public Set<XcodeSdk> parse(String content) {
				dev.nokee.runtime.darwin.internal.parsers.XcodebuildSdksLexer lexer = new dev.nokee.runtime.darwin.internal.parsers.XcodebuildSdksLexer(CharStreams.fromString(content));
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				dev.nokee.runtime.darwin.internal.parsers.XcodebuildSdksParser parser = new dev.nokee.runtime.darwin.internal.parsers.XcodebuildSdksParser(tokens);

				ExtractSdkModel l = new ExtractSdkModel();
				ParseTreeWalker.DEFAULT.walk((ParseTreeListener)l, parser.output());

				return l.allSdks;
			}
		};
	}

	private static class ExtractSdkModel extends dev.nokee.runtime.darwin.internal.parsers.XcodebuildSdksBaseListener {
		Set<XcodeSdk> allSdks = new LinkedHashSet<>();

		@Override
		public void exitSdkIdentifier(dev.nokee.runtime.darwin.internal.parsers.XcodebuildSdksParser.SdkIdentifierContext ctx) {
			allSdks.add(new XcodeSdk(ctx.getText()));
		}
	}
}
