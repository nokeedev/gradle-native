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

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

public final class LinkCheckerExtension implements TestTemplateInvocationContextProvider {
	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		if (!context.getTestMethod().isPresent()) {
			return false;
		}

		Method testMethod = context.getTestMethod().get();
		if (!isAnnotated(testMethod, LinkCheck.class)) {
			return false;
		}

		return true;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		Supplier<Path> baseDirectoryProviderMethod = AnnotationUtils.findAnnotation(context.getRequiredTestMethod(), LinkCheck.class)
			.map(it -> it.value())
			.map(it -> ReflectionUtils.newInstance(it))
			.orElseThrow(RuntimeException::new);

		// @formatter:off
		return Stream.of(baseDirectoryProviderMethod.get())
			.flatMap(path -> {
				GenericContainer<?> host;
				host = new GenericContainer<>("nginx:alpine").withFileSystemBind(path.toString(), "/usr/share/nginx/html", BindMode.READ_ONLY).withExposedPorts(80);
				host.start();
				host.waitingFor(new HostPortWaitStrategy());
				Set<URI> foundLinks = new HashSet<>();

				Set<String> l = new HashSet<>();
				try {
					Files.walkFileTree(path, new FileVisitor<Path>() {
						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							l.add("http://localhost:" + host.getMappedPort(80) + "/" + path.relativize(file));
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
							return FileVisitResult.TERMINATE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				new Crawler().crawl(l, new Crawler.Visitor() {
					@Override
					public List<URI> visit(URI url, Document document) {
						return document.select("a")// get elements with "a" tag to find links
							.stream()
							.parallel()
							.map(element -> element.getElementsByAttribute("href")) //map href elements
							.map(elements -> elements.attr("href"))
							.filter(link -> !link.isEmpty()) //filter the non-empty links
							.filter(link -> !link.contains("javascript") && !link.contains("*&")) // filter other patterns
							.map(link -> link.trim())
							.map(link -> url.resolve(link))
							.distinct() //remove duplicate data
							.peek(foundLinks::add)
							.filter(link -> link.getHost().equals("localhost"))
							// TODO: Normalize URL (remove anchors...)
							.collect(Collectors.toList());
					}
				});
				return foundLinks.stream();
			})
			.map(LinkCheckInvocationContext::new);
		// @formatter:on
	}

	public static final class LinkCheckInvocationContext implements TestTemplateInvocationContext {
		private final URI link;

		public LinkCheckInvocationContext(URI link) {
			this.link = link;
		}

		@Override
		public String getDisplayName(int invocationIndex) {
			return TestTemplateInvocationContext.super.getDisplayName(invocationIndex) + " check " + link;
		}

		@Override
		public List<Extension> getAdditionalExtensions() {
			return Collections.singletonList(new LinkCheckParameterResolver(link));
		}
	}

	public static final class LinkCheckParameterResolver implements ParameterResolver {
		private final URI link;

		public LinkCheckParameterResolver(URI link) {
			this.link = link;
		}

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
			return parameterContext.getParameter().getType().equals(URI.class);
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
			return link;
		}
	}
}
