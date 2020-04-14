package dev.nokee.docs.fixtures;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import lombok.NonNull;
import lombok.Value;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TreeCommandHelper {
	@Value
	public static class Output {
		@NonNull List<String> paths;

		public static TreeCommandHelper.Output parse(String output) {
			dev.nokee.docs.fixtures.TreeLexer lexer = new dev.nokee.docs.fixtures.TreeLexer(CharStreams.fromString(output));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			dev.nokee.docs.fixtures.TreeParser parser = new dev.nokee.docs.fixtures.TreeParser(tokens);


			TreeCommandHelper.ExtractTreeModel l = new TreeCommandHelper.ExtractTreeModel();
			ParseTreeWalker.DEFAULT.walk((ParseTreeListener)l, parser.output());

			return new TreeCommandHelper.Output(l.allPath);
		}
	}

	private static class ExtractTreeModel extends dev.nokee.docs.fixtures.TreeBaseListener {
		final List<String> allPath = new ArrayList<>();
		final Deque<String> currentPath = new ArrayDeque<>();
		String lastPath = "";
		int currentLevel = 0;

		@Override
		public void enterTreeHeader(dev.nokee.docs.fixtures.TreeParser.TreeHeaderContext ctx) {
			lastPath = ctx.getText();
		}

		@Override
		public void enterEntry(dev.nokee.docs.fixtures.TreeParser.EntryContext ctx) {
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
			String path = String.join("/", Iterables.concat(currentPath, ImmutableList.of(lastPath)));
			allPath.add(path);
		}
	}
}
