package patmal.workshops.student.impl.app;

import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.student.impl.logic.WorkshopSystemImpl;

public class Main {
    public static void main(String[] args) {
        WorkshopSystem system = new WorkshopSystemImpl();
        system.createInstructor("i1", "Dana Levi", "dana.levi@univ.edu");
        System.out.println("Instructors: " + system.getInstructors().size());
    }
}