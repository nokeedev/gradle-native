package dev.gradleplugins.exemplarkit.asciidoc;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.*;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import static org.asciidoctor.OptionsBuilder.options;

public final class AsciidoctorContent {
	private final ContentNode contentNode;

	private AsciidoctorContent(ContentNode contentNode) {
		this.contentNode = contentNode;
	}

	public static AsciidoctorContent load(File documentFile) {
		return load(documentFile, it -> {});
	}

	public static AsciidoctorContent load(File documentFile, Consumer<OptionsBuilder> action) {
		try (Asciidoctor asciidoctor = Asciidoctor.Factory.create()) {
			OptionsBuilder options = options();
			action.accept(options);

			Document document = asciidoctor.loadFile(documentFile, options.asMap());
			return new AsciidoctorContent(document);
		}
	}

	public static AsciidoctorContent of(Document document) {
		return new AsciidoctorContent(document);
	}

	public void walk(Visitor visitor) {
		Deque<ContentNode> queue = new ArrayDeque<>();
		queue.add(contentNode);
		while (!queue.isEmpty()) {
			ContentNode node = queue.poll();
			if (node instanceof Document) {
				visitor.visit((Document) node);
			} else if (node instanceof Block) {
				visitor.visit((Block) node);
			}

			if (node instanceof List) {
				addAllFirst(queue, ((org.asciidoctor.ast.List) node).getItems());
			} else if (node instanceof StructuralNode) {
				addAllFirst(queue, ((StructuralNode) node).getBlocks());
			}
		}
	}

	private static void addAllFirst(Deque<ContentNode> queue, java.util.List<? extends ContentNode> items) {
		for (int i = items.size() - 1; i >= 0; i--) {
			queue.addFirst(items.get(i));
		}
	}

	public interface Visitor {
		void visit(Document node);
		void visit(Block node);
	}
}
