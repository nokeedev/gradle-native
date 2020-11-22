package dev.gradleplugins.exemplarkit.asciidoc;

import dev.gradleplugins.exemplarkit.Step;
import org.asciidoctor.ast.Document;

import java.io.File;
import java.util.List;

public final class AsciidocExemplarStepLoader {
	public static List<Step> extractFromAsciiDoc(File adocFile) {
		AsciidoctorExemplarStepExtractor extractor = new AsciidoctorExemplarStepExtractor();
		AsciidoctorContent.load(adocFile).walk(extractor);
		return extractor.get();
	}

	public static List<Step> extractFromAsciiDoc(Document document) {
		AsciidoctorExemplarStepExtractor extractor = new AsciidoctorExemplarStepExtractor();
		AsciidoctorContent.of(document).walk(extractor);
		return extractor.get();
	}
}
