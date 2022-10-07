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
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LinkCheckerUtils {
	public static Stream<URI> findAllLinks(Path path) {
		GenericContainer<?> host;
		host = new GenericContainer<>("nginx:alpine").withFileSystemBind(path.toString(), "/usr/share/nginx/html", BindMode.READ_ONLY).withExposedPorts(80);
		host.start();
		host.waitingFor(new HostPortWaitStrategy());

		System.out.println("HOST RUNNING? " + host.isRunning());
		System.out.println("HOST HEALTHY? " + host.isHealthy());
		Set<URI> foundLinks = new HashSet<>();

		Set<String> l = new HashSet<>();
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					System.out.println("FOUND " + path);
					l.add("http://localhost:" + host.getMappedPort(80) + "/" + path.relativize(file));
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		System.out.println("CRAWLING " + l);

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
		System.out.println("FOUND LINKS " + foundLinks);
		return foundLinks.stream();
	}
}
