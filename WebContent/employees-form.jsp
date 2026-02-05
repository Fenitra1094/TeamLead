<!-- employees-form.jsp -->
<form action="save-employees" method="POST">
    <h3>Department</h3>
    Department ID: <input type="number" name="deptId" value="100"><br>
    Dept Name: <input type="text" name="name" value="IT Department"><br>
    Dept Code: <input type="text" name="code" value="IT"><br>
    
    <h3>Employees (tableau)</h3>
    
    <h4>Employee 1</h4>
    Name: <input type="text" name="employees[0].name" value="Jean Dupont"><br>
    Position: <input type="text" name="employees[0].position" value="Developer"><br>
    Salary: <input type="number" step="0.01" name="employees[0].salary" value="50000"><br>
    
    <h4>Employee 2</h4>
    Name: <input type="text" name="employees[1].name" value="Marie Curie"><br>
    Position: <input type="text" name="employees[1].position" value="Manager"><br>
    Salary: <input type="number" step="0.01" name="employees[1].salary" value="75000"><br>
    
    <h4>Employee 3</h4>
    Name: <input type="text" name="employees[2].name" value="Paul Smith"><br>
    Position: <input type="text" name="employees[2].position" value="Analyst"><br>
    Salary: <input type="number" step="0.01" name="employees[2].salary" value="60000"><br>
    
    <input type="submit" value="Sauvegarder">
</form>