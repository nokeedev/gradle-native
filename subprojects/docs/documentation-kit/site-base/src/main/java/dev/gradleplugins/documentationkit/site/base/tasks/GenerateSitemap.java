package dev.gradleplugins.documentationkit.site.base.tasks;

import dev.gradleplugins.documentationkit.site.base.Sitemap;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;

import static java.nio.file.Files.newBufferedWriter;

// See https://www.sitemaps.org/
// You can ping google and bing via http://<host>/ping?sitemap=https://nokee.dev/sitemap.xml
public abstract class GenerateSitemap extends DefaultTask {
	@Input
	public abstract SetProperty<Sitemap.Url> getSitemapUrls();

	@OutputFile
	public abstract RegularFileProperty getGeneratedSitemapFile();

	@TaskAction
	private void doGenerate() throws IOException, XMLStreamException {
		try (final SitemapWriter writer = new SitemapWriter(newBufferedWriter(getGeneratedSitemapFile().get().getAsFile().toPath()))) {
			writer.write(new Sitemap(getSitemapUrls().get()));
		}
	}

	private static final class SitemapWriter implements AutoCloseable {
		private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		private final XMLStreamWriter writer;

		private SitemapWriter(Writer writer) throws XMLStreamException {
			this.writer = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
		}

		public void write(Sitemap sitemap) throws XMLStreamException {
			writer.writeStartDocument("1.0");
			writer.writeStartElement("urlset");
			writer.writeNamespace("xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9");

			for (Sitemap.Url url : sitemap) {
				writeUrl(url);
			}

			writer.writeEndElement(); // urlset
			writer.writeEndDocument();

			writer.flush();
		}

		private void writeUrl(Sitemap.Url url) throws XMLStreamException {
			writer.writeStartElement("url");

			writer.writeStartElement("loc");
			writer.writeCharacters(url.getLocation().toString());
			writer.writeEndElement(); // loc

			if (url.getLastModified() != null) {
				writer.writeStartElement("lastmod");
				writer.writeCharacters(DATE_FORMAT.format(url.getLastModified()));
				writer.writeEndElement(); // lastmod
			}

			writer.writeEndElement(); // url
		}

		@Override
		public void close() throws XMLStreamException {
			writer.close();
		}
	}
}
