package dev.nokee.buildadapter.cmake.internal.fileapi;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.LoggingEngine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

public final class CodeModelClientImpl implements CodeModelClient {
	private final Supplier<CommandLineTool> cmakeSupplier;

	public CodeModelClientImpl(Supplier<CommandLineTool> cmakeSupplier) {
		this.cmakeSupplier = cmakeSupplier;
	}

	@Override
	public CodeModelReplyFiles query(File baseDirectory) {
		try {
			// Query CMake using file API
			val cmakeFileApiDirectory = new File(baseDirectory, ".cmake/api/v1");
			val cmakeFileApiQueryCodemodelFile = new File(cmakeFileApiDirectory, "query/client-gradle/codemodel-v2");

			// TODO: check if Gradle already reset the files
			//  if so, maybe we should do an out of source build to ensure build artifacts are kept
			if (cmakeFileApiDirectory.exists()) {
				FileUtils.deleteDirectory(cmakeFileApiDirectory);
			}

			// Create query file
			cmakeFileApiQueryCodemodelFile.getParentFile().mkdirs();
			cmakeFileApiQueryCodemodelFile.createNewFile();

			// TODO: Check cmake tool can be found
			// TODO: Check version supports File API: starting with cmake 3.14 before the feature doesn't exists
			cmakeSupplier.get().withArguments(".").newInvocation().workingDirectory(baseDirectory).buildAndSubmit(LoggingEngine.wrap(new ProcessBuilderEngine())).waitFor().assertNormalExitValue();

			val cmakeFileApiReplyDirectory = new File(cmakeFileApiDirectory, "reply");
			return CodeModelReplyFiles.of(cmakeFileApiReplyDirectory);
		} catch (IOException e) {
			return rethrow(e);
		}
	}


}
