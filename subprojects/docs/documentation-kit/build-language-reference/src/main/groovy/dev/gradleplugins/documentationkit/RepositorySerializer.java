package dev.gradleplugins.documentationkit;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.CamelCaseStyle;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.stream.Style;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class RepositorySerializer {
	public void serialize(List<URI> repositories, File outputFile) throws Exception {
		Style style = new CamelCaseStyle(false, false);
		Format format = new Format(3, "<?xml version=\"1.0\" encoding=\"utf-8\"?>", style);
		Registry registry = new Registry();
		Strategy strategy = new RegistryStrategy(registry);
		Serializer serializer = new Persister(strategy, format);
		serializer.write(Repositories.of(repositories), outputFile);
	}

	public List<URI> deserialize(File inputFile) throws Exception {
		Style style = new CamelCaseStyle(false, false);
		Format format = new Format(3, "<?xml version=\"1.0\" encoding=\"utf-8\"?>", style);
		Registry registry = new Registry();
		Strategy strategy = new RegistryStrategy(registry);
		Serializer serializer = new Persister(strategy, format);

		List<URI> list = new ArrayList<>();
		for (String it : serializer.read(Repositories.class, inputFile).get()) {
			URI uri = new URI(it);
			list.add(uri);
		}
		return list;
	}

	@Root
	private static final class Repositories {
		@ElementList(entry = "repository", inline = true, type = String.class)
		private List<String> repositories;

		public Repositories() {}

		public Repositories(List<String> repositories) {
			this.repositories = repositories;
		}

		public static Repositories of(List<URI> repositories) {
			return new Repositories(repositories.stream().map(URI::toString).collect(Collectors.toList()));
		}

		public List<String> get() {
			return repositories;
		}
	}
}
