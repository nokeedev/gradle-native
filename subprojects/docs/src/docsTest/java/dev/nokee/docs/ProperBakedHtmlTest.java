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
package dev.nokee.docs;

import com.google.common.collect.MoreCollectors;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import dev.nokee.docs.fixtures.ClassSource;
import dev.nokee.docs.fixtures.StreamMatchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static dev.nokee.docs.fixtures.HttpRequestMatchers.contentAttribute;
import static dev.nokee.docs.fixtures.HttpRequestMatchers.document;
import static dev.nokee.docs.fixtures.HttpRequestMatchers.fullSentence;
import static dev.nokee.docs.fixtures.HttpRequestMatchers.hasAltText;
import static dev.nokee.docs.fixtures.HttpRequestMatchers.hrefAttribute;
import static dev.nokee.docs.fixtures.HttpRequestMatchers.nameOrPropertyAttribute;
import static dev.nokee.docs.fixtures.HttpRequestMatchers.notListToString;
import static dev.nokee.docs.fixtures.HttpRequestMatchers.reasonableLength;
import static dev.nokee.docs.fixtures.HttpRequestMatchers.relAttribute;
import static dev.nokee.docs.fixtures.HttpRequestMatchers.typeAttribute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.blankString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;

@Tag("Baked")
class ProperBakedHtmlTest {
	@ParameterizedTest(name = "has alt test on all images [{0}]")
	@ClassSource(AllHtmlWithoutJavadocSupplier.class)
	void hasAltTextOnAllImages(Path path, URI uri) {
		assertThat(document(uri).select("img"), everyItem(hasAltText()));
	}

	@ParameterizedTest(name = "has proper canonical links [{0}]")
	@ClassSource(AllHtmlWithoutJavadocSupplier.class)
	void hasProperCanonicalLinks(Path path, URI uri) {
		assertThat(document(uri).select("link").stream().filter(relAttribute("canonical")),
			StreamMatchers.yieldsExactly(hrefAttribute(equalTo("https://docs.nokee.dev/" + asCanonicalPath(path)))));
	}

	@ParameterizedTest(name = "has proper open-graph URL [{0}]")
	@ClassSource(AllHtmlWithoutJavadocSupplier.class)
	void hasProperOpenGraphUrl(Path path, URI uri) {
		assertThat(document(uri).select("meta").stream().filter(nameOrPropertyAttribute("og:url")),
			StreamMatchers.yieldsExactly(contentAttribute(equalTo("https://docs.nokee.dev/" + asCanonicalPath(path)))));
	}

	private static String asCanonicalPath(Path path) {
		assert !path.isAbsolute();
		System.out.println(path);
		if (path.toString().equals("index.html")) {
			return "manual/user-manual.html";
		} else if (path.toString().equals("manual/index.html")) {
			return "manual/user-manual.html";
		} else if (path.getFileName().toString().equals("index.html")) {
			return path.getParent().toString() + "/";
		}
		return path.toString();
	}

	@ParameterizedTest(name = "has proper description [{0}]")
	@ClassSource(AllHtmlWithoutJavadocSupplier.class)
	void hasProperDescription(Path path, URI uri) {
		// It seems 160 characters is a good limit
		assertThat(document(uri).select("meta").stream().filter(nameOrPropertyAttribute("description")),
			StreamMatchers.yieldsExactly(contentAttribute(allOf(fullSentence(), reasonableLength()))));
	}

	@ParameterizedTest(name = "has proper keywords [{0}]")
	@ClassSource(AllHtmlWithoutJavadocSupplier.class)
	void hasProperKeywords(Path path, URI uri) {
		assertThat(document(uri).select("meta").stream().filter(nameOrPropertyAttribute("keywords")),
			StreamMatchers.yieldsExactly(contentAttribute(allOf(
				not(blankString()), // should have keywords
				notListToString() // guard against baking mistake
			))));
	}

	@ParameterizedTest(name = "has proper twitter meta description [{0}]")
	@ClassSource(AllHtmlWithoutJavadocSupplier.class)
	void hasProperTwitterMetaDescription(Path path, URI uri) {
		assertThat(document(uri).select("meta").stream().filter(nameOrPropertyAttribute("twitter:description")), StreamMatchers.yieldsExactly(contentAttribute(allOf(fullSentence(), reasonableLength()))));
	}

	public static final class AllHtmlWithoutJavadocSupplier implements Supplier<Stream<Arguments>> {
		@Override
		public Stream<Arguments> get() {
			List<Arguments> result = new ArrayList<>();
			Path baseDirectory = new File(System.getProperty("bakedContentDirectory")).toPath();
			try {
				Files.walkFileTree(baseDirectory, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						if (dir.getFileName().toString().equals("javadoc")) {
							return FileVisitResult.SKIP_SUBTREE;
						} else {
							return FileVisitResult.CONTINUE;
						}
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if (file.getFileName().toString().endsWith(".html")) {
							result.add(Arguments.of(baseDirectory.relativize(file), file.toUri()));
						}
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			return result.stream();
		}
	}

	@Disabled
	@ParameterizedTest(name = "has breadcrumb structured data [{0}]")
	@ClassSource(AllHtmlWithoutJavadocSupplier.class)
	void hasBreadcrumbStructuredData(Path path, URI uri) {
		Assumptions.assumeFalse(path.toString().equals("index.thml"), "ignores redirect page");
		Assumptions.assumeFalse(path.toString().equals("manual/index.thml"), "ignores redirect page");

		BreadcrumbList breadcrumb = new Gson().fromJson(document(uri).select("script").stream().filter(typeAttribute("application/ld+json")).collect(MoreCollectors.onlyElement()).text(), BreadcrumbList.class);

		assertThat(breadcrumb.getContext(), equalTo("https://schema.org"));
		assertThat(breadcrumb.getType(), equalTo("BreadcrumbList"));
		assertThat(breadcrumb.getItemListElement(), iterableWithSize(greaterThan(0)));
		// TODO: figure out breadcrumbs
	}

	public static final class BreadcrumbList {
		@SerializedName("@context")
		private final String context;
		@SerializedName("@type")
		private final String type;
		private final List<ListItem> itemListElement;

		public BreadcrumbList(String context, String type, List<ListItem> itemListElement) {
			this.context = context;
			this.type = type;
			this.itemListElement = itemListElement;
		}

		public String getContext() {
			return context;
		}

		public String getType() {
			return type;
		}

		public List<ListItem> getItemListElement() {
			return itemListElement;
		}

		public static final class ListItem {
			@SerializedName("@type")
			private final String type;
			private final int position;
			private final String name;
			private final String item;

			public ListItem(String type, int position, String name, String item) {
				this.type = type;
				this.position = position;
				this.name = name;
				this.item = item;
			}

			public String getType() {
				return type;
			}

			public int getPosition() {
				return position;
			}

			public String getName() {
				return name;
			}

			public String getItem() {
				return item;
			}
		}
	}
}
