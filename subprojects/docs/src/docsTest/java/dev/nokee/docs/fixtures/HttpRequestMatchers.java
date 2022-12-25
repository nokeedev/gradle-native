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

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.docs.fixtures.StringMatchers.hasLength;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

public final class HttpRequestMatchers {
	private HttpRequestMatchers() {}

	public static Matcher<URI> statusCode(int value) {
		return statusCode(equalTo(value));
	}

	public static Matcher<URI> statusCode(Matcher<? super Integer> matcher) {
		return new FeatureMatcher<URI, Integer>(matcher, "", "") {
			@Override
			protected Integer featureValueOf(URI actual) {
				try {
					URL url = actual.toURL();
					HttpURLConnection http = (HttpURLConnection) url.openConnection();
					http.setRequestProperty("User-Agent", "curl/7.64.1");
					http.setConnectTimeout(15000);
					http.setReadTimeout(15000);
					http.setRequestMethod("GET"); //this is important, some websites don't allow head request
					http.disconnect();
					return http.getResponseCode();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	public static Document document(URI uri) {
		try {
			Document doc = null;
			if (uri.getScheme().equals("file")) {
				return Jsoup.parse(new File(uri));
			} else {
				Connection connection = Jsoup.connect(uri.toString());
				return connection.get();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Predicate<Element> nameOrPropertyAttribute(String value) {
		return element -> element.attr("name").equals(value) || element.attr("property").equals(value);
	}

	public static Predicate<Element> relAttribute(String value) {
		return element -> element.attr("rel").equals(value);
	}

	public static Predicate<Element> typeAttribute(String value) {
		return element -> element.attr("type").equals(value);
	}

	public static Matcher<Element> contentAttribute(Matcher<? super String> matcher) {
		return new FeatureMatcher<Element, String>(matcher, "'content' attribute", "'content' attribute") {
			@Override
			protected String featureValueOf(Element actual) {
				return actual.attr("content");
			}
		};
	}

	public static Matcher<Element> hrefAttribute(Matcher<? super String> matcher) {
		return new FeatureMatcher<Element, String>(matcher, "", "") {
			@Override
			protected String featureValueOf(Element actual) {
				return actual.attr("href");
			}
		};
	}

	public static Matcher<Element> hasAltText() {
		return new FeatureMatcher<Element, String>(not(blankOrNullString()), "", "") {
			@Override
			protected String featureValueOf(Element actual) {
				return actual.attr("alt");
			}
		};
	}

	public static Matcher<String> fullSentence() {
		return anyOf(endsWith("."), endsWith("!"));
	}

	public static Matcher<CharSequence> reasonableLength() {
		return hasLength(allOf(greaterThan(0), lessThanOrEqualTo(160)));
	}

	public static Matcher<String> notListToString() {
		return not(allOf(startsWith("["), endsWith("]")));
	}
}
