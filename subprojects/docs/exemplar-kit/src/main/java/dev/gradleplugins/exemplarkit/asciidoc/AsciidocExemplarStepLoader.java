/*
 * Copyright 2020 the original author or authors.
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
