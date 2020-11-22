package dev.gradleplugins.exemplarkit.fixtures

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile

class ExemplarSample {
	private final SourceElement delegate = new SourceElement() {
		@Override
		List<SourceFile> getFiles() {
			return [sourceFile('', 'hello', '''#!/usr/bin/env bash
echo Hello, world!
''')]
		}
	}

	void writeToDirectory(File directory) {
		delegate.writeToSourceDir(directory)
	}

	void writeToDirectoryAsArchive() {

	}
}
