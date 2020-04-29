package dev.nokee.docs.fixtures;

import lombok.NonNull;
import lombok.Value;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.HashSet;
import java.util.Set;

public class JarCommandHelper {
	@Value
	public static class Output {
		@NonNull Set<String> paths;

		public static JarCommandHelper.Output parse(String output) {
			dev.nokee.docs.fixtures.JarLexer lexer = new dev.nokee.docs.fixtures.JarLexer(CharStreams.fromString(output));
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			dev.nokee.docs.fixtures.JarParser parser = new dev.nokee.docs.fixtures.JarParser(tokens);

			ExtractJarModel l = new ExtractJarModel();
			ParseTreeWalker.DEFAULT.walk((ParseTreeListener)l, parser.output());

			return new JarCommandHelper.Output(l.allPaths);
		}
	}

	private static class ExtractJarModel extends dev.nokee.docs.fixtures.JarBaseListener {
		Set<String> allPaths = new HashSet<>();
		@Override
		public void enterPath(dev.nokee.docs.fixtures.JarParser.PathContext ctx) {
			allPaths.add(ctx.getText());
		}
	}
}
