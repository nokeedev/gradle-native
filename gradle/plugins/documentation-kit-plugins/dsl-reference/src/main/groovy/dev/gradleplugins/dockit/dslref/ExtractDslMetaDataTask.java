/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.gradleplugins.dockit.dslref;

import com.github.javaparser.JavaParser;
import dev.gradleplugins.dockit.DocGenerationException;
import dev.gradleplugins.dockit.dsl.source.SourceMetaDataVisitor;
import dev.gradleplugins.dockit.dsl.source.TypeNameResolver;
import dev.gradleplugins.dockit.dsl.source.model.ClassMetaData;
import dev.gradleplugins.dockit.dsl.source.model.TypeMetaData;
import dev.gradleplugins.dockit.model.ClassMetaDataRepository;
import dev.gradleplugins.dockit.model.SimpleClassMetaDataRepository;
import groovy.time.TimeCategory;
import groovy.time.TimeDuration;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import java.io.File;
import java.util.Date;

/**
 * Extracts meta-data from the Groovy and Java source files which make up the Gradle API. Persists the meta-data to a file
 * for later use in generating documentation for the DSL, such as by {@link dev.gradleplugins.dockit.dsl.docbook.AssembleDslDocTask}.
 */
@CacheableTask
public abstract class ExtractDslMetaDataTask extends SourceTask {
	@OutputFile
	public abstract RegularFileProperty getDestinationFile();

	/**
	 * {@inheritDoc}
	 */
	@Override
	@PathSensitive(PathSensitivity.NAME_ONLY)
	public FileTree getSource() {
		return super.getSource();
	}

	@TaskAction
	private void extract() {
		Date start = new Date();

		//parsing all input files into metadata
		//and placing them in the repository object
		SimpleClassMetaDataRepository<ClassMetaData> repository = new SimpleClassMetaDataRepository<ClassMetaData>();
		int counter = 0;
		for (File f : getSource()) {
			parse(f, repository);
			counter++;
		}

		//updating/modifying the metadata and making sure every type reference across the metadata is fully qualified
		//so, the superClassName, interfaces and types needed by declared properties and declared methods will have fully qualified name
		TypeNameResolver resolver = new TypeNameResolver(repository);
		repository.each(new Action<ClassMetaData>() {
			@Override
			public void execute(ClassMetaData metaData) {
				fullyQualifyAllTypeNames(metaData, resolver);
			}
		});
		repository.store(getDestinationFile().get().getAsFile());

		Date stop = new Date();
		TimeDuration elapsedTime = TimeCategory.minus(stop, start);
		System.out.println(String.format("Parsed %d classes in %s", counter, elapsedTime));
	}

	void parse(File sourceFile, ClassMetaDataRepository<ClassMetaData> repository) {
		if (!sourceFile.getName().endsWith(".java")) {
			throw new DocGenerationException("Parsing non-Java files is not supported: " + sourceFile.getAbsolutePath());
		}
		try {
			new SourceMetaDataVisitor().visit(new JavaParser().parse(sourceFile).getResult().get(), repository);
		} catch (Exception e) {
			throw new DocGenerationException("Could not parse '$sourceFile'.", e);
		}
	}

	void fullyQualifyAllTypeNames(ClassMetaData classMetaData, TypeNameResolver resolver) {
		try {
			classMetaData.resolveTypes(new Transformer<String, String>() {
				public String transform(String i) {
					return resolver.resolve(i, classMetaData);
				}
			});
			classMetaData.visitTypes(new Action<TypeMetaData>() {
				public void execute(TypeMetaData t) {
					resolver.resolve(t, classMetaData);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("Could not resolve types in class '" + classMetaData.getClassName() + "'.", e);
		}
	}
}
