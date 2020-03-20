package dev.nokee.docs.fixtures;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.LinkedHashSet;
import java.util.Set;

public class UnzipCommandHelper {
	@Value
	public static class Output {
		@NonNull String archivePath;
		@NonNull Set<ArchiveAction> actions;

		public static Output parse(String output) {
			dev.nokee.docs.fixtures.UnzipLexer lexer = new dev.nokee.docs.fixtures.UnzipLexer(CharStreams.fromString(output));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			dev.nokee.docs.fixtures.UnzipParser parser = new dev.nokee.docs.fixtures.UnzipParser(tokens);

			ExtractUnzipModel l = new ExtractUnzipModel();
			ParseTreeWalker.DEFAULT.walk((ParseTreeListener)l, parser.output());

			return new Output(l.getArchivePath(), l.getActions());
		}
	}

	private enum ArchiveOperation {
		INFLATING, CREATING
	}

	@Value
	private static class ArchiveAction {
		@NonNull ArchiveOperation operation;
		@NonNull String path;
	}

	private static class ExtractUnzipModel extends dev.nokee.docs.fixtures.UnzipBaseListener {
		@Getter private final Set<ArchiveAction> actions = new LinkedHashSet<>();
		@Getter private String archivePath;

		@Override
		public void exitUnzipHeader(dev.nokee.docs.fixtures.UnzipParser.UnzipHeaderContext ctx) {
			archivePath = ctx.Path().getText();
		}

		@Override
		public void exitCreateAction(dev.nokee.docs.fixtures.UnzipParser.CreateActionContext ctx) {
			actions.add(new ArchiveAction(ArchiveOperation.CREATING, ctx.Path().getText()));
		}

		@Override
		public void exitInflateAction(dev.nokee.docs.fixtures.UnzipParser.InflateActionContext ctx) {
			actions.add(new ArchiveAction(ArchiveOperation.INFLATING, ctx.Path().getText()));
		}
	}
}
