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
package dev.gradleplugins.exemplarkit.output;

import dev.gradleplugins.exemplarkit.output.UnzipCommandOutputParser.CreateActionContext;
import dev.gradleplugins.exemplarkit.output.UnzipCommandOutputParser.ExtractActionContext;
import dev.gradleplugins.exemplarkit.output.UnzipCommandOutputParser.InflateActionContext;
import dev.gradleplugins.exemplarkit.output.UnzipCommandOutputParser.UnzipHeaderContext;
import lombok.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.LinkedHashSet;
import java.util.Set;

@EqualsAndHashCode
@RequiredArgsConstructor
public final class UnzipCommandOutput {
	private final String archivePath;
	private final Set<ArchiveAction> actions;
	@EqualsAndHashCode.Exclude private final String output;

	public String get() {
		return output;
	}

	public static UnzipCommandOutput from(String output) {
		UnzipCommandOutputLexer lexer = new UnzipCommandOutputLexer(CharStreams.fromString(output));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		UnzipCommandOutputParser parser = new UnzipCommandOutputParser(tokens);

		ExtractUnzipModel l = new ExtractUnzipModel();
		ParseTreeWalker.DEFAULT.walk((ParseTreeListener)l, parser.output());

		return new UnzipCommandOutput(l.getArchivePath(), l.getActions(), output);
	}

	private enum ArchiveOperation {
		INFLATING, CREATING
	}

	@Value
	private static class ArchiveAction {
		@NonNull ArchiveOperation operation;
		@NonNull String path;
	}

	private static class ExtractUnzipModel extends UnzipCommandOutputBaseListener {
		@Getter private final Set<ArchiveAction> actions = new LinkedHashSet<>();
		@Getter private String archivePath;

		@Override
		public void exitUnzipHeader(UnzipHeaderContext ctx) {
			archivePath = ctx.Path().getText();
		}

		@Override
		public void exitCreateAction(CreateActionContext ctx) {
			actions.add(new ArchiveAction(ArchiveOperation.CREATING, ctx.Path().getText()));
		}

		@Override
		public void exitInflateAction(InflateActionContext ctx) {
			actions.add(new ArchiveAction(ArchiveOperation.INFLATING, ctx.Path().getText()));
		}

		@Override
		public void exitExtractAction(ExtractActionContext ctx) {
			actions.add(new ArchiveAction(ArchiveOperation.INFLATING, ctx.Path().getText()));
		}
	}
}
