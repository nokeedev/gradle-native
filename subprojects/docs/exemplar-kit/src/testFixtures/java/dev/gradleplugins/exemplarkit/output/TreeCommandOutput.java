package dev.gradleplugins.exemplarkit.output;

import dev.gradleplugins.exemplarkit.output.TreeCommandOutputParser.EntryContext;
import dev.gradleplugins.exemplarkit.output.TreeCommandOutputParser.TreeHeaderContext;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@EqualsAndHashCode
@RequiredArgsConstructor
public final class TreeCommandOutput {
	final List<String> paths;
	@EqualsAndHashCode.Exclude private final String output;

	public String get() {
		return output;
	}

	public static TreeCommandOutput from(String output) {
		TreeCommandOutputLexer lexer = new TreeCommandOutputLexer(CharStreams.fromString(output));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		TreeCommandOutputParser parser = new TreeCommandOutputParser(tokens);


		ExtractTreeModel l = new ExtractTreeModel();
		ParseTreeWalker.DEFAULT.walk((ParseTreeListener)l, parser.output());

		return new TreeCommandOutput(l.allPath, output);
	}

	private static class ExtractTreeModel extends TreeCommandOutputBaseListener {
		final List<String> allPath = new ArrayList<>();
		final Deque<String> currentPath = new ArrayDeque<>();
		String lastPath = "";
		int currentLevel = 0;

		@Override
		public void enterTreeHeader(TreeHeaderContext ctx) {
			lastPath = ctx.getText();
		}

		@Override
		public void enterEntry(EntryContext ctx) {
			int level = ctx.level().Indent().size();
			if (level > currentLevel) {
				currentPath.addLast(lastPath);
			}

			while (level < currentLevel) {
				currentPath.removeLast();
				currentLevel--;
			}

			lastPath = ctx.element().getText();
			currentLevel = level;

			val pathSegment = new ArrayList<String>();
			pathSegment.addAll(currentPath);
			pathSegment.add(lastPath);
			String path = String.join("/", pathSegment);
			allPath.add(path);
		}
	}
}
