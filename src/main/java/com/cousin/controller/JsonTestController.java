package com.cousin.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.Json;
import com.cousin.model.Emp;
import java.util.*;

@Controller
public class JsonTestController {

    @GetMapping("/json/emp")
    @Json
    public Emp getEmp() {
        Emp e = new Emp();
        e.setNom("Dupont");
        e.setPrenom("Jean");
        e.setAge(42);
        return e;
    }

    @GetMapping("/json/emps")
    @Json
    public List<Emp> listEmps() {
        Emp e1 = new Emp(); e1.setNom("A"); e1.setPrenom("P1"); e1.setAge(30);
        Emp e2 = new Emp(); e2.setNom("B"); e2.setPrenom("P2"); e2.setAge(25);
        return Arrays.asList(e1, e2);
    }
}
