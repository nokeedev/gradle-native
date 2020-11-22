package dev.gradleplugins.exemplarkit.output;

import lombok.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JarCommandOutput {
	private final Set<String> paths;
	@EqualsAndHashCode.Exclude private final String output;

	public String get() {
		return output;
	}

	public static JarCommandOutput from(String output) {
		dev.gradleplugins.exemplarkit.output.JarCommandOutputLexer lexer = new dev.gradleplugins.exemplarkit.output.JarCommandOutputLexer(CharStreams.fromString(output));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		dev.gradleplugins.exemplarkit.output.JarCommandOutputParser parser = new dev.gradleplugins.exemplarkit.output.JarCommandOutputParser(tokens);

		ExtractJarModel l = new ExtractJarModel();
		ParseTreeWalker.DEFAULT.walk((ParseTreeListener)l, parser.output());

		return new JarCommandOutput(l.allPaths, output);
	}

	private static class ExtractJarModel extends dev.gradleplugins.exemplarkit.output.JarCommandOutputBaseListener {
		Set<String> allPaths = new HashSet<>();
		@Override
		public void enterPath(dev.gradleplugins.exemplarkit.output.JarCommandOutputParser.PathContext ctx) {
			allPaths.add(ctx.getText());
		}
	}
}
