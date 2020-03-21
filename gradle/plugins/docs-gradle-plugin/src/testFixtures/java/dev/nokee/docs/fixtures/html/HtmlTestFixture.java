package dev.nokee.docs.fixtures.html;

import com.google.common.collect.Sets;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.NodeChild;
import lombok.NonNull;
import lombok.Value;
import org.cyberneko.html.parsers.SAXParser;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Value
public class HtmlTestFixture {
	Path root;
	@NonNull URI uri;
	@NonNull UriService uriService;

	public <T extends HtmlTagFixture> Set<T> findAll(HtmlTag<T> tag) {
		SAXParser parser = new SAXParser();
		try {
			GPathResult page = new XmlSlurper(parser).parseText(uriService.fetch(uri));
			Spliterator<GPathResult> it = Spliterators.spliteratorUnknownSize(page.depthFirst(), Spliterator.NONNULL);
			Set<T> result = Sets.newLinkedHashSet();
			result.addAll(StreamSupport.stream(it, false)
					.filter(tag::is)
					.map(e -> tag.create(uri, (NodeChild)e))
					.collect(Collectors.toSet()));
			return result;
		} catch (IOException | SAXException e) {
			throw new RuntimeException(e);
		}
	}
}
