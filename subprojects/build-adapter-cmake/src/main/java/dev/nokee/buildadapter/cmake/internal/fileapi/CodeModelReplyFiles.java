package dev.nokee.buildadapter.cmake.internal.fileapi;

import lombok.val;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

public final class CodeModelReplyFiles {
	private final File indexFile;

	public CodeModelReplyFiles(File indexFile) {
		this.indexFile = indexFile;
	}

	public static CodeModelReplyFiles of(File replyDirectory) {
		try (val stream = Files.newDirectoryStream(replyDirectory.toPath(), CodeModelReplyFiles::findIndexReplyFile)) {
			val iter = stream.iterator();
			if (!iter.hasNext()) {
				throw new IllegalStateException("No response from cmake");
			}

			val indexFile = iter.next().toFile();

			if (iter.hasNext()) {
				// TODO: Maybe we could warn instead
				throw new IllegalStateException("Too many response file...");
			}

			return new CodeModelReplyFiles(indexFile);
		} catch (IOException e) {
			return rethrow(e);
		}
	}

	private static boolean findIndexReplyFile(Path entry) {
		return entry.getFileName().toString().startsWith("index-");
	}

	public void visit(Visitor visitor) {
		val index = IndexReplyFile.of(indexFile);
		visitor.visit(index);
		index.get().getObjects().forEach(objectReference -> {
			val codeModel = CodeModelReplyFile.of(new File(indexFile.getParentFile(), objectReference.getJsonFile()));
			visitor.visit(codeModel);

			codeModel.get().getConfigurations().stream().flatMap(it -> it.getTargets().stream()).forEach(targetReference -> {
				val target = CodeModelTargetReplyFile.of(new File(indexFile.getParentFile(), targetReference.getJsonFile()));
				visitor.visit(target);
			});
		});
	}

	public interface Visitor {
		void visit(CodeModelReplyFile codeModel);

		void visit(CodeModelTargetReplyFile codeModelTarget);

		void visit(IndexReplyFile replyIndex);
	}
}
