<% if (!content.classMethods.empty) { %>
== Method Details

<% content.classMethods.each { method ->
def parameters = method.metaData.parameters.collect { "${linkBuilder.link(it.type).render(renderer)} ${it.name}" }.join(', ')
%>
[[${method.id.replace('(', '-').replace(')', '-').replace(', ', '-').replace('[', '-').replace(']', '-')}]]
=== ${linkBuilder.link(method.metaData.returnType).render(renderer)} ${method.name}(${parameters})

${method.comment.render(renderer)}
<% } %>

<% } %>
