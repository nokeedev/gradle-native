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
package dev.nokee.docs.fixtures;

import dev.gradleplugins.exemplarkit.asciidoc.AsciidoctorContent;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.asciidoctor.OptionsBuilder.options;

public final class NokeeReadMe {
	private final Path location;
	private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();
	private final Document readme;

	public NokeeReadMe(Path location) {
		this.location = location;
		readme = asciidoctor.loadFile(location.toFile(), options().asMap());
	}

	public List<SnippetBlock> findSnippetBlocks() {
		List<SnippetBlock> snippets = new ArrayList<>();
		AsciidoctorContent.of(readme).walk(new AsciidoctorContent.Visitor() {
			@Override
			public void visit(Document node) {}

			@Override
			public void visit(Block node) {
				if (node.isBlock() && node.getContext().equals("listing") && node.getStyle().equals("source")) {
					switch (node.getAttribute("file").toString()) {
						case "build":
							snippets.add(new SnippetBlock() {
								@Override
								public Type getType() {
									return Type.BUILD;
								}

								@Override
								public String getContent() {
									return node.getContent().toString();
								}
							});
							break;
						case "settings":
							snippets.add(new SnippetBlock() {
								@Override
								public Type getType() {
									return Type.SETTINGS;
								}

								@Override
								public String getContent() {
									return node.getContent().toString();
								}
							});
							break;
						default:
							throw new RuntimeException("Unrecognized snippet block");
					}
				}
			}
		});
		return snippets;
	}

	public interface SnippetBlock {
		enum Type { BUILD, SETTINGS }
		Type getType();

		String getContent();
	}

	public String getNokeeVersion() {
		return readme.getAttributes().get("jbake-version").toString();
	}
}
