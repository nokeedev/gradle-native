package dev.nokee.docs

import dev.gradleplugins.exemplarkit.asciidoc.AsciidoctorContent
import dev.gradleplugins.runnerkit.GradleExecutor
import dev.gradleplugins.runnerkit.GradleRunner
import dev.gradleplugins.test.fixtures.gradle.GradleScriptDsl
import dev.nokee.docs.fixtures.html.HtmlLinkTester
import groovy.json.JsonSlurper
import org.asciidoctor.Asciidoctor
import org.asciidoctor.ast.Block
import org.asciidoctor.ast.Document
import org.asciidoctor.ast.StructuralNode
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.environment.OperatingSystem

import java.nio.file.Files

import static dev.nokee.docs.fixtures.html.HtmlLinkTester.validEmails
import static org.asciidoctor.OptionsBuilder.options

class ReadmeTest extends Specification {
	private static final String README_LOCATION_PROPERTY_NAME = 'dev.nokee.docs.readme.location'
	@Shared Asciidoctor asciidoctor = Asciidoctor.Factory.create()
	@Shared Document readme = asciidoctor.loadFile(readmeFile, options().asMap())

	private static File getReadmeFile() {
		assert System.properties.containsKey(README_LOCATION_PROPERTY_NAME)
		return new File(System.getProperty(README_LOCATION_PROPERTY_NAME))
	}

	def "uses the latest released version"() {
		expect:
		readme.attributes.get('jbake-version') == currentNokeeVersion
	}

	private static String getCurrentNokeeVersion() {
		return new JsonSlurper().parse(new URL('https://services.nokee.dev/versions/current.json')).version
	}

	@Requires({ os.family == OperatingSystem.Family.MAC_OS })
	def "checks for broken links"() {
		given:
		def rootDirectory = Files.createTempDirectory('nokee')
		def renderedReadMeFile = rootDirectory.resolve('readme.html').toFile()
		renderedReadMeFile.text = asciidoctor.convertFile(readmeFile, options().toFile(false))

		expect:
		def report = new HtmlLinkTester(validEmails("hello@nokee.dev"), new HtmlLinkTester.BlackList() {
			@Override
			boolean isBlackListed(URI uri) {
				if (uri.scheme == 'file') {
					def path = rootDirectory.toUri().relativize(uri).toString()
					if (new File(readmeFile.parentFile, path).exists()) {
						return true
					}
				}
				return false
			}
		}).reportBrokenLinks(rootDirectory.toFile())
		report.assertNoFailures()
	}

	@Unroll
	def "check code snippet"(dsl) {
		given:
		def rootDirectory = Files.createTempDirectory('nokee')
		def settingsFile = rootDirectory.resolve(dsl.settingsFileName).toFile()
		def buildFile = rootDirectory.resolve(dsl.buildFileName).toFile()
		findSnippetBlocks().each {
			switch (it.getAttribute("file").toString()) {
				case "build":
					buildFile << it.content
					break
				case "settings":
					settingsFile << it.content
					break
				default:
					throw new RuntimeException("Unrecognized snippet block")
			}
		}

		and:
		GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(rootDirectory.toFile()).withGradleVersion("6.2.1").withGradleUserHomeDirectory(rootDirectory.resolve('gradle-user-home').toFile())

		expect:
		runner.withArgument('help').build()
		runner.withArgument('tasks').build()

		where:
		dsl << [GradleScriptDsl.GROOVY_DSL, GradleScriptDsl.KOTLIN_DSL]
	}

	List<StructuralNode> findSnippetBlocks() {
		def snippets = new ArrayList<StructuralNode>();
		AsciidoctorContent.of(readme).walk(new AsciidoctorContent.Visitor() {
			@Override
			void visit(Document node) {}

			@Override
			void visit(Block node) {
				if (node.isBlock() && node.getContext().equals("listing") && node.getStyle().equals("source")) {
					snippets.add(node)
				}
			}
		})
		return snippets
	}
}
