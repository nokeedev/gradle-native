package dev.nokee.buildadapter.cmake.internal.fileapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
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

	private Index loadIndex() {
		try {
			return new Gson().fromJson(FileUtils.readFileToString(indexFile, Charset.defaultCharset()), Index.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e); // TODO
		}
	}

	private CodeModel loadCodeModel(String filename) {
		try {
			return new Gson().fromJson(FileUtils.readFileToString(new File(indexFile.getParentFile(), filename), Charset.defaultCharset()), CodeModel.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e); // TODO
		}
	}

	private CodeModelTarget loadCodeModelTarget(String filename) {
		try {
			return new Gson().fromJson(FileUtils.readFileToString(new File(indexFile.getParentFile(), filename), Charset.defaultCharset()), CodeModelTarget.class);
		} catch (IOException e) {
			throw new UncheckedIOException(e); // TODO
		}
	}

	public void visit(Visitor visitor) {
		val index = loadIndex();
		visitor.visit(index);
		index.getObjects().forEach(objectReference -> {
			val codeModel = loadCodeModel(objectReference.getJsonFile());
			visitor.visit(codeModel);

			codeModel.getConfigurations().stream().flatMap(it -> it.getTargets().stream()).forEach(targetReference -> {
				val target = loadCodeModelTarget(targetReference.getJsonFile());
				visitor.visit(target);
			});
		});
	}

	public interface Visitor {
		void visit(CodeModel codeModel);

		void visit(CodeModelTarget codeModelTarget);

		void visit(Index replyIndex);
	}
}
