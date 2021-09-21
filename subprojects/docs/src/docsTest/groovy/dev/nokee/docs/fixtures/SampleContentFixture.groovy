/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.docs.fixtures

import dev.gradleplugins.exemplarkit.Exemplar
import dev.gradleplugins.exemplarkit.Step
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.gradle.GradleScriptDsl
import dev.nokee.docs.fixtures.html.HtmlTestFixture
import dev.nokee.docs.fixtures.html.UriService
import org.asciidoctor.Asciidoctor
import org.asciidoctor.ast.Document

import static dev.gradleplugins.exemplarkit.asciidoc.AsciidocExemplarStepLoader.extractFromAsciiDoc
import static org.asciidoctor.OptionsBuilder.options

class SampleContentFixture {
	private final File contentFile
	private final String sampleName
	private Document document

	SampleContentFixture(String sampleName) {
		this.sampleName = sampleName
		def contentDirectory = new File(System.getProperty('sampleContentDirectory'))
		this.contentFile = new File(contentDirectory, "${sampleName}/index.adoc")
	}

	private Document loadDocument() {
		if (document == null) {
			Asciidoctor asciidoctor = Asciidoctor.Factory.create()
			document = asciidoctor.loadFile(contentFile, options().asMap())
		}
		return document
	}

	TestFile getGroovyDslSample() {
		Document document = loadDocument()
//		processAsciidocSampleBlocks(document)
		return TestFile.of(new File("${System.getProperty('sampleArchiveDirectory')}/${document.attributes.get('jbake-archivebasename')}-${document.attributes.get('jbake-version')}-groovy-dsl.zip")).assertExists()
	}

	TestFile getKotlinDslSample() {
		Document document = loadDocument()
//		processAsciidocSampleBlocks(document)
		return TestFile.of(new File("${System.getProperty('sampleArchiveDirectory')}/${document.attributes.get('jbake-archivebasename')}-${document.attributes.get('jbake-version')}-kotlin-dsl.zip")).assertExists()
	}

	TestFile getDslSample(GradleScriptDsl dsl) {
		if (dsl == GradleScriptDsl.GROOVY_DSL) {
			return getGroovyDslSample()
		}
		return getKotlinDslSample()
	}

	Exemplar getDslExemplar(GradleScriptDsl dsl) {
		def builder = Exemplar.builder()
		for (Step step : extractFromAsciiDoc(loadDocument())) {
			builder = builder.step(step)
		}
		if (dsl == GradleScriptDsl.GROOVY_DSL) {
			return builder.fromArchive(getGroovyDslSample()).build()
		}
		return builder.fromArchive(getKotlinDslSample()).build()
	}

	String getCategory() {
		Document document = loadDocument()
		return (String)document.attributes.get('jbake-category')
	}

	String getSummary() {
		Document document = loadDocument()
		return (String)document.attributes.get('jbake-summary')
	}

	HtmlTestFixture getBakedFile() {
		def root = new File(System.getProperty('bakedContentDirectory')).toPath()
		return new HtmlTestFixture(root, root.resolve("samples/${sampleName}/index.html").toUri(), UriService.INSTANCE)
	}
}
