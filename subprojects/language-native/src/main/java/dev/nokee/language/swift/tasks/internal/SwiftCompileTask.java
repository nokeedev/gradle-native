package dev.nokee.language.swift.tasks.internal;

import dev.nokee.language.swift.tasks.SwiftCompile;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.internal.file.Deleter;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;

import javax.inject.Inject;

@CacheableTask
public abstract class SwiftCompileTask extends org.gradle.language.swift.tasks.SwiftCompile implements SwiftCompile {
	@Inject
	public SwiftCompileTask(CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, Deleter deleter) {
		super(compilerOutputFileNamingSchemeFactory, deleter);
	}
}
