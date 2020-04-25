<% if (!content.classProperties.empty) { %>
== Property Details

<% content.classProperties.each { property -> %>
[[${property.id}]]
=== ${linkBuilder.link(property.metaData.type).render(renderer)} ${property.name} ${property.readOnly ? '(read-only)' : property.writeOnly ? '(write-only)' : '' }

${property.comment.render(renderer)}
<% } %>

<% } %>
