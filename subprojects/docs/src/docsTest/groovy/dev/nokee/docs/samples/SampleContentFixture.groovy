package dev.nokee.docs.samples

import dev.gradleplugins.test.fixtures.file.TestFile
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

//	private static void processAsciidocSampleBlocks(StructuralNode rootNode) throws IOException {
//		Path tempDir = Files.createTempDirectory("exemplar-testable-samples");
//
//		Queue<StructuralNode> queue = new ArrayDeque<>();
//		queue.add(rootNode);
//		while (!queue.isEmpty()) {
//			StructuralNode node = queue.poll();
//
//
//			List<StructuralNode> blocks = node.getBlocks();
//			// Some asciidoctor AST types return null instead of an empty list
//			if (blocks == null) {
//				continue;
//			}
//
//			for (StructuralNode child : blocks) {
//				if (child.isBlock() && child.hasRole("testable-sample")) {
//					List<Command> commands = extractAsciidocCommands(node);
//					// Nothing to verify, skip this sample
//					if (commands.isEmpty()) {
//						// TODO: print a warning here as this is probably a user mistake
//						continue;
//					}
//					samples.add(processSampleNode(child, tempDir, commands));
//				} else {
//					queue.offer(child);
//				}
//			}
//		}
//		return samples;
//	}
}
