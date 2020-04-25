== Properties

<% if (content.classProperties.empty) { %>
No properties.
<% } else { %>

[cols="1,2", options="header", width=100%]
|===
|Property
|Description

<% content.classProperties.each { property -> %>
|link:#${property.id}[${property.name}]
|${property.description}
<% } %>
|===

<% } %>
