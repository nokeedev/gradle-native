package dev.gradleplugins.documentationkit.site.base;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Iterator;

public final class Sitemap implements Iterable<Sitemap.Url>{
	private final Collection<Url> urls;

	public Sitemap(Collection<Url> urls) {
		this.urls = urls;
	}

	@Override
	public Iterator<Url> iterator() {
		return urls.iterator();
	}

	public static final class Url implements Serializable {
		private final URL location;
		private final LocalDate lastModified;

		public Url(URL location, LocalDate lastModified) {
			this.location = location;
			this.lastModified = lastModified;
		}

		public URL getLocation() {
			return location;
		}

		public LocalDate getLastModified() {
			return lastModified;
		}
	}
}
