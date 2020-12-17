package dev.gradleplugins.exemplarkit.asciidoc;

import dev.gradleplugins.exemplarkit.Step;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.gradleplugins.exemplarkit.asciidoc.AsciidocCalloutNormalizer.normalize;

final class AsciidoctorExemplarStepExtractor implements AsciidoctorContent.Visitor {
	private static final String COMMAND_PREFIX = "$ ";
	private final List<Step> steps = new ArrayList<>();

	@Override
	public void visit(Document node) {

	}

	@Override
	public void visit(Block node) {
		if (node.isBlock() && node.hasRole("terminal")) {
			parseEmbeddedCommand(node);
		}
	}

	private void parseEmbeddedCommand(Block block) {
		Map<String, Object> attributes = new HashMap<>(block.getAttributes());
		String[] lines = block.getSource().split("\r?\n");
		int pos = 0;

		do {
			pos = parseOneCommand(lines, pos, attributes);
		} while (pos < lines.length);
	}

	private int parseOneCommand(String[] lines, int pos, Map<String, Object> attributes) {
		String commandLineString = lines[pos];
		if (!commandLineString.startsWith(COMMAND_PREFIX)) {
			throw new RuntimeException("Inline sample command " + commandLineString);
		}

		AsciidocCommandLineParser commandLine = AsciidocCommandLineParser.parse(normalize(commandLineString.substring(COMMAND_PREFIX.length())));

		StringBuilder expectedOutput = new StringBuilder();
		int nextCommand = pos + 1;
		while (nextCommand < lines.length && !lines[nextCommand].startsWith(COMMAND_PREFIX)) {
			if (nextCommand > pos + 1) {
				expectedOutput.append("\n");
			}
			expectedOutput.append(lines[nextCommand]);
			nextCommand++;
		}


		steps.add(
			Step.builder()
				.execute(commandLine.getExecutable(), commandLine.getArguments())
				.output(normalize(expectedOutput.toString()))
				.attributes(attributes)
				.build());
		return nextCommand;
	}

	public List<Step> get() {
		return steps;
	}
}
