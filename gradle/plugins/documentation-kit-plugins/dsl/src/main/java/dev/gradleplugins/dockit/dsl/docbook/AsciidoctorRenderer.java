package dev.gradleplugins.dockit.dsl.docbook;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class AsciidoctorRenderer implements Renderer<String> {
	private int literalCount = 0;

	public AsciidoctorRenderer() {
	}

	public String render(List<Element> elements) {
		return elements.stream().map(this::render).collect(Collectors.joining());
	}

	private String render(Node e) {
		switch (e.getNodeName()) {
			case "para":
				return render(e.getChildNodes()) + "\n\n";
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
			case "programlisting":
				return String.format("[.listing]\n----\n%s\n----\n", render(e.getChildNodes()));
			case "#text":
				return e.getTextContent();
			case "literal":
				literalCount++;
				try {
					if (literalCount == 1) {
						return "`" + render(e.getChildNodes()) + "`";
					}
					return render(e.getChildNodes());
				} finally {
					literalCount--;
				}
			case "emphasis":
				return "*" + render(e.getChildNodes()) + "*";
			case "blockquote":
				return Arrays.stream(StringUtils.stripStart(render(e.getChildNodes()), null).split("\n", -1)).map(it -> "> " + it).collect(Collectors.joining("\n")) + "\n\n";
			case "classname":
			default:
				return render(e.getChildNodes());
		}
	}

	private String render(NodeList list) {
		return IntStream.range(0, list.getLength()).mapToObj(list::item).map(this::render).collect(Collectors.joining());
	}

	@Override
	public String render(Renderable obj) {
		if (literalCount != 0) {
			System.out.println(String.format("Literal count not zero! (%d)", literalCount));
			literalCount = 0;
		}
		if (obj instanceof DocComment) {
			return render(((DocComment) obj).getDocbook());
		} else if (obj instanceof DocLink) {
			return render(((DocLink) obj).getDocbook());
		} else {
			throw new IllegalArgumentException("unknown renderable type");
		}
	}
}
