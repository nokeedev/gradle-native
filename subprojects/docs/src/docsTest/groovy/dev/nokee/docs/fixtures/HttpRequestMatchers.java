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

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import static org.hamcrest.Matchers.equalTo;

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
}
