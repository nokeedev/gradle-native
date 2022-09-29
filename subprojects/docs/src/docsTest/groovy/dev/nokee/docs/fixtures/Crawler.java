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

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class Crawler {
	private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private static URI uri(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public void crawl(Set<String> urls, Visitor visitor) {
		CrawlRequest req = new CrawlRequest(visitor);
		urls.stream().map(Crawler::uri).forEach(req::submit);
		req.await();
	}

	private final class CrawlRequest {
		private final Set<URI> alreadyVisited = Collections.synchronizedSet(new HashSet<>());
		private final Visitor visitor;
		private final Deque<Future<?>> results = new ConcurrentLinkedDeque<>();

		private CrawlRequest(Visitor visitor) {
			this.visitor = visitor;
		}

		public void submit(URI uri) {
			if (alreadyVisited.add(uri)) {
				results.add(executor.submit(new CrawlRunnable(uri)));
			}
		}

		public void await() {
			while (!results.isEmpty()) {
				Future<?> r = results.pop();
				while (!r.isDone()) {
					Thread.yield();
				}
			}
		}

		public final class CrawlRunnable implements Runnable {
			private final URI next;

			public CrawlRunnable(URI next) {
				this.next = next;
			}

			@Override
			public void run() {
				Connection connection = Jsoup.connect(next.toString());
				try {
					Document doc = connection.get();
					visitor.visit(next, doc).stream().forEach(CrawlRequest.this::submit);
				} catch (IOException e) {
					// TODO: Visit as failure
					throw new UncheckedIOException(e);
				}
			}

			@Override
			public String toString() {
				return "crawl '" + next + "'";
			}
		}
	}

	public interface Visitor {
		List<URI> visit(URI url, Document document);
	}
}
