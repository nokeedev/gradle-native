== Methods
<% if (content.classMethods.empty) { %>
No methods.
<% } else { %>

[cols="1,2", options="header", width=100%]
|===
|Method
|Description

<% content.classMethods.each { property ->
def parameters = property.metaData.parameters*.name.join(', ')
%>
|`link:#${property.id.replace('(', '-').replace(')', '-').replace(', ', '-')}[${property.name}](${parameters})`
|${property.description}
<% } %>
|===

<% } %>
