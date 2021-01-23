package dev.gradleplugins.documentationkit.site.base.tasks;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import dev.gradleplugins.documentationkit.site.base.Sitemap;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// See https://www.sitemaps.org/
// You can ping google and bing via http://<host>/ping?sitemap=https://nokee.dev/sitemap.xml
public abstract class GenerateSitemap extends DefaultTask {
	@Input
	public abstract SetProperty<Sitemap.Url> getSitemapUrls();

	@OutputFile
	public abstract RegularFileProperty getGeneratedSitemapFile();

	@TaskAction
	private void doGenerate() throws IOException {
		XmlMapper xmlMapper = new XmlMapper();
		SimpleModule simpleModule = new SimpleModule("LocalDateAsYearMonthDayString", new Version(1, 0, 0, null, null, null));
		simpleModule.addSerializer(LocalDate.class, new SitemapLocalDateSerializer());
		xmlMapper.registerModule(simpleModule);
		xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
		xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
		String xml = xmlMapper.writeValueAsString(new Sitemap(getSitemapUrls().get()));

		try (val outStream = new FileOutputStream(getGeneratedSitemapFile().get().getAsFile())) {
			outStream.write(xml.getBytes(StandardCharsets.UTF_8));
		}
	}


	private static class SitemapLocalDateSerializer extends JsonSerializer<LocalDate> {
		private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		@Override
		public void serialize(LocalDate value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
			jgen.writeString(DATE_FORMAT.format(value));
		}
	}
}
