package dev.nokee.docs.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.gradle.GradleScriptDsl
import dev.nokee.docs.fixtures.html.HtmlTestFixture
import dev.nokee.docs.fixtures.html.UriService
import org.asciidoctor.Asciidoctor
import org.asciidoctor.ast.Document

import static org.asciidoctor.OptionsBuilder.options

class SampleContentFixture {
	private final File contentFile
	private final String sampleName

	SampleContentFixture(String sampleName) {
		this.sampleName = sampleName
		def contentDirectory = new File(System.getProperty('sampleContentDirectory'))
		this.contentFile = new File(contentDirectory, "${sampleName}/index.adoc")
	}

	TestFile getGroovyDslSample() {
		Asciidoctor asciidoctor = Asciidoctor.Factory.create()
		Document document = asciidoctor.loadFile(contentFile, options().asMap())
//		processAsciidocSampleBlocks(document)
		return TestFile.of(new File("${System.getProperty('sampleArchiveDirectory')}/${document.attributes.get('jbake-archivebasename')}-${document.attributes.get('jbake-version')}-groovy-dsl.zip")).assertExists()
	}

	TestFile getKotlinDslSample() {
		Asciidoctor asciidoctor = Asciidoctor.Factory.create()
		Document document = asciidoctor.loadFile(contentFile, options().asMap())
//		processAsciidocSampleBlocks(document)
		return TestFile.of(new File("${System.getProperty('sampleArchiveDirectory')}/${document.attributes.get('jbake-archivebasename')}-${document.attributes.get('jbake-version')}-kotlin-dsl.zip")).assertExists()
	}

	TestFile getDslSample(GradleScriptDsl dsl) {
		if (dsl == GradleScriptDsl.GROOVY_DSL) {
			return getGroovyDslSample()
		}
		return getKotlinDslSample()
	}

	List<Command> getCommands() {
		Asciidoctor asciidoctor = Asciidoctor.Factory.create()
		Document document = asciidoctor.loadFile(contentFile, options().asMap())
		return new CommandDiscovery().extractAsciidocCommands(document)
	}

	String getCategory() {
		Asciidoctor asciidoctor = Asciidoctor.Factory.create()
		Document document = asciidoctor.loadFile(contentFile, options().asMap())
		return (String)document.attributes.get('jbake-category')
	}

	String getSummary() {
		Asciidoctor asciidoctor = Asciidoctor.Factory.create()
		Document document = asciidoctor.loadFile(contentFile, options().asMap())
		return (String)document.attributes.get('jbake-summary')
	}

	HtmlTestFixture getBakedFile() {
		def root = new File(System.getProperty('bakedContentDirectory')).toPath()
		return new HtmlTestFixture(root, root.resolve("docs/nightly/samples/${sampleName}/index.html").toUri(), UriService.INSTANCE)
	}
}
