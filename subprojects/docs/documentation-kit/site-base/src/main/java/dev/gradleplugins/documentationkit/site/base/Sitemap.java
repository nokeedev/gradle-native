package dev.gradleplugins.documentationkit.site.base;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

import java.io.Serializable;
import java.net.URL;
import java.time.LocalDate;
import java.util.Collection;

@Value
@JacksonXmlRootElement(localName = "urlset")
public class Sitemap {
	@JacksonXmlProperty(isAttribute = true)
	@Getter(AccessLevel.PRIVATE) String xmlns = "https://www.sitemaps.org/schemas/sitemap/0.9";

	@JacksonXmlElementWrapper(useWrapping = false)
	@JacksonXmlProperty(localName = "url")
	Collection<Url> urls;

	@Value
	public static class Url implements Serializable {
		@JacksonXmlProperty(localName = "loc")
		URL location;

		@JacksonXmlProperty(localName = "lastmod")
		LocalDate lastModified;
	}
}
