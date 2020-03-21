package dev.nokee.docs.fixtures.html;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import groovyx.net.http.HttpBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * A service for general URI operations for this link tester.
 */
public abstract class UriService {
	public static final UriService INSTANCE = new CachingUriService();

	/**
	 * Checks if the resource exists.
	 * @param uri resource to check
	 * @return {@code true} if the resource exists or {@code false} otherwise.
	 */
	public abstract boolean exists(URI uri);

	/**
	 * Fetches the resource as a text format.
	 * @param uri resource to fetch
	 * @return the resource as String or throw an exception.
	 */
	public abstract String fetch(URI uri);

	/**
	 * Checks the SSL certificate of the host the resource.
	 * @param uri resource to validate the SSL certificate
	 * @return {@code true} if the resource support SSL or {@code false} otherwise.
	 */
	public abstract boolean hasValidSslCertificate(URI uri);


	/**
	 * Default implementation.
	 */
	private static class DefaultUriService extends UriService {
		public boolean exists(URI uri) {
			HttpBuilder client = HttpBuilder.configure(config -> {
				config.getRequest().setUri(uri);

				Map<String, CharSequence> headers = new HashMap<>();
				headers.put("User-Agent", "nokee-labs/0.0.0.1");
				config.getRequest().setHeaders(headers);
			});
			try {
				client.head();
				return true;
			} catch (RuntimeException e) {
				return false;
			}
		}

		public boolean hasValidSslCertificate(URI uri) {
			HttpBuilder client = HttpBuilder.configure(config -> {
				try {
					config.getRequest().setUri(new URI("https", uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null));
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}

				Map<String, CharSequence> headers = new HashMap<>();
				headers.put("User-Agent", "nokee-labs/0.0.0.1");
				config.getRequest().setHeaders(headers);
			});
			try {
				client.head();
				return true;
			} catch (RuntimeException e) {
				return false;
			}
		}

		public String fetch(URI uri) {
			try {
				URLConnection connection = uri.toURL().openConnection();
				connection.addRequestProperty("User-Agent", "Non empty");

				return IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
			} catch (IOException e) {
				throw new UncheckedIOException(String.format("Unable to load resource content '%s'", uri), e);
			}
		}
	}

	/**
	 * Caching implementation.
	 */
	private static class CachingUriService extends UriService {
		private final UriService delegate = new DefaultUriService();
		private final LoadingCache<URI, Boolean> headCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build(
				new CacheLoader<URI, Boolean>() {
					public Boolean load(URI key) {
						return delegate.exists(key);
					}
				});
		private final LoadingCache<URI, String> getCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.build(
				new CacheLoader<URI, String>() {
					public String load(URI key) {
						return delegate.fetch(key);
					}
				});

		@Override
		public boolean exists(URI uri) {
			return headCache.getUnchecked(uri);
		}

		@Override
		public String fetch(URI uri) {
			return getCache.getUnchecked(uri);
		}

		@Override
		public boolean hasValidSslCertificate(URI uri) {
			return delegate.hasValidSslCertificate(uri);
		}
	}
}
