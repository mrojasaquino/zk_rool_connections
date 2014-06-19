package com.ecaresoft.cp.db.entity;

import com.ecaresoft.cp.db.DB;
import java.util.List;

/**
 *
 * @author odelarosa
 */
public class Student extends Entity {

    private String name;
    private String lastName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public static void main(String[] args) {
        // Crear estudiante 1
        Student student = new Student();
        student.setName("Omar");
        student.setLastName("de la Rosa");

        // Guardar estudiante
        student.save();

        // Crear estudiante 2
        Student student1 = new Student();
        student1.setName("Jose");
        student1.setLastName("Perez");

        // Guardar estudiante
        student1.save();

        // Listar todos los estudiantes
        List<Student> list = DB.get(Student.class);

        System.out.println("Listado de estudiantes");

        for (Student s : list) {
            System.out.print("\t");
            System.out.println(s);
        }

        // Buscar usuario por id
        System.out.println("Buscar por Id " + student.getId());
        System.out.print("\t");
        System.out.println(DB.get(student.getId(), Student.class));

        // Actualizar datos del estudiante
        System.out.println("Actualizando de " + student1.getLastName() + " a Perez Perez");
        student1.setLastName("Perez Perez");

        // Actualizar datos del estudiante
        student1.save();

        // Listar nuevamente los estudiantes
        list = DB.get(Student.class);

        System.out.println("Listado de estudiantes");

        for (Student s : list) {
            System.out.print("\t");
            System.out.println(s);
        }
    }

    @Override
    public String toString() {
        return getName() + " " + getLastName();
    }
}
