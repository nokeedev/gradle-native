package dev.nokee.docs.fixtures;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.jruby.ast.impl.ListImpl;

import java.util.*;

public class CommandDiscovery {
	private static final String COMMAND_PREFIX = "$ ";

	public static List<Command> extractAsciidocCommands(StructuralNode testableSampleBlock) {
		List<Command> commands = new ArrayList<>();
		Queue<StructuralNode> queue = new ArrayDeque<>();
		queue.add(testableSampleBlock);
		while (!queue.isEmpty()) {
			StructuralNode node = queue.poll();
			if (node instanceof ListImpl) {
				queue.addAll(((ListImpl) node).getItems());
			} else {
				for (StructuralNode child : node.getBlocks()) {
					if (child.isBlock() && child.hasRole("terminal")) {
						parseEmbeddedCommand((Block) child, commands);
					} else {
						queue.offer(child);
					}
				}
			}
		}

		return commands;
	}

	private static void parseEmbeddedCommand(Block block, List<Command> commands) {
		Map<String, Object> attributes = block.getAttributes();
		String[] lines = block.getSource().split("\r?\n");
		int pos = 0;

		do {
			pos = parseOneCommand(lines, pos, attributes, commands);
		} while (pos < lines.length);
	}

	private static int parseOneCommand(String[] lines, int pos, Map<String, Object> attributes, List<Command> commands) {
		String commandLineString = lines[pos];
		if (!commandLineString.startsWith(COMMAND_PREFIX)) {
			throw new RuntimeException("Inline sample command " + commandLineString);
		}

		CommandLine commandLine = CommandLine.of(commandLineString.substring(COMMAND_PREFIX.length()));

		StringBuilder expectedOutput = new StringBuilder();
		int nextCommand = pos + 1;
		while (nextCommand < lines.length && !lines[nextCommand].startsWith(COMMAND_PREFIX)) {
			if (nextCommand > pos + 1) {
				expectedOutput.append("\n");
			}
			expectedOutput.append(lines[nextCommand]);
			nextCommand++;
		}

		Command command = new Command(commandLine,
			Optional.<String>empty(),
			Collections.<String>emptyList(),
			Optional.of(expectedOutput.toString()),
			attributes.containsKey("expect-failure"),
			attributes.containsKey("allow-additional-output"),
			attributes.containsKey("allow-disordered-output"),
			Collections.emptyList());
		commands.add(command);
		return nextCommand;
	}
}
