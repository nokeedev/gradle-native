package dev.nokee.docs.dsl.docbook;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class AsciidoctorRenderer implements Renderer<String> {
	public static final AsciidoctorRenderer INSTANCE = new AsciidoctorRenderer();

	private AsciidoctorRenderer() {
	}

	public String render(List<Element> elements) {
		return elements.stream().map(AsciidoctorRenderer::render).collect(Collectors.joining());
	}

	private static String render(Node e) {
		switch (e.getNodeName()) {
			case "para":
				return "\n\n" + render(e.getChildNodes()) + "\n\n";
			case "ulink":
				String url = e.getAttributes().getNamedItem("url").getTextContent();
				return "link:" + url + "[" + render(e.getChildNodes()) + "]";
			case "apilink":
				String className = e.getAttributes().getNamedItem("class").getTextContent();
				Node methodNode = e.getAttributes().getNamedItem("method");
				if (methodNode == null) {
					return String.format("link:../javadoc/%s.html[%s]", className.replace('.', '/'), StringUtils.substringAfterLast(className, "."));
				}
				return String.format("link:../javadoc/%s.html#%s[%s]", className.replace('.', '/'), methodNode.getTextContent().replace('(', '-').replace(')', '-'), StringUtils.substringAfterLast(className, "."));
			default:
				if (e.getChildNodes().getLength() > 0) {
					return render(e.getChildNodes());
				}
				return e.getTextContent();
		}
	}

	private static String render(NodeList list) {
		return IntStream.range(0, list.getLength()).mapToObj(list::item).map(AsciidoctorRenderer::render).collect(Collectors.joining());
	}

	@Override
	public String render(Renderable obj) {
		if (obj instanceof DocComment) {
			return render(((DocComment) obj).getDocbook());
		} else if (obj instanceof DocLink) {
			return render(((DocLink) obj).getDocbook());
		} else {
			throw new IllegalArgumentException("unknown renderable type");
		}
	}
}
