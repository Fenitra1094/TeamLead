<!-- employees-result.jsp -->
<%@ page import="com.app.models.Employee, com.app.models.Department" %>
<%@ page import="java.util.Arrays" %>
<%
    Employee[] employees = (Employee[]) request.getAttribute("employees");
    Department department = (Department) request.getAttribute("department");
    Integer deptId = (Integer) request.getAttribute("deptId");
%>

<h1>✅ Tableau d'objets réussi !</h1>

<h2>Department</h2>
<p>ID: <%= deptId %></p>
<p>Name: <%= department != null ? department.getName() : "null" %></p>
<p>Code: <%= department != null ? department.getCode() : "null" %></p>

<h2>Employees (<%= employees != null ? employees.length : 0 %>)</h2>
<% if (employees != null && employees.length > 0) { %>
<table border="1" cellpadding="10">
    <tr>
        <th>#</th>
        <th>Name</th>
        <th>Position</th>
        <th>Salary</th>
    </tr>
    <% for (int i = 0; i < employees.length; i++) { 
        Employee emp = employees[i];
        if (emp != null) {
    %>
    <tr>
        <td><%= i %></td>
        <td><%= emp.getName() %></td>
        <td><%= emp.getPosition() %></td>
        <td>$<%= String.format("%,.2f", emp.getSalary()) %></td>
    </tr>
    <%   }
       } %>
</table>
<% } else { %>
<p>❌ Aucun employee reçu</p>
<% } %>