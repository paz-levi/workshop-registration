package patmal.workshops.student.impl.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.logic.WorkshopSystemImpl;

import java.io.File;
import java.io.UncheckedIOException;
import java.util.List;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);
    private static final String DEFAULT_CSV = "students.csv";
    private static final String USAGE = "Usage: java -jar workshop-cli.jar [students.csv]";

    public static void main(String[] args) {
        WorkshopSystem system = new WorkshopSystemImpl();

        // One instructor with two workshops
        Instructor dana = system.createInstructor("i1", "Dana Levi", "dana.levi@univ.edu");
        Workshop javaWorkshop = system.createWorkshop("Java Workshop", "Introduction to Java");
        Workshop mavenWorkshop = system.createWorkshop("Maven Workshop", "Building projects with Maven");
        system.associateWorkshopWithInstructor(dana, javaWorkshop);
        system.associateWorkshopWithInstructor(dana, mavenWorkshop);

        // Students come from a CSV file
        String path = args.length > 0 ? args[0] : DEFAULT_CSV;
        File source = new File(path);
        if (!source.isFile()) {
            LOG.error("Student file not found: {}", source.getAbsolutePath());
            LOG.error(USAGE);
            System.exit(1);
        }
        List<Student> imported;
        try {
            imported = system.importStudentDataFromFile(source);
        } catch (IllegalArgumentException | UncheckedIOException e) {
            LOG.error("Cannot import students from {}: {}", path, e.getMessage());
            System.exit(1);
            return;
        }
        LOG.info("Imported {} students from {}", imported.size(), source.getAbsolutePath());

        // Two teams; s4 stays free
        system.createTeam("Alpha", javaWorkshop, List.of("s1", "s2"));
        system.createTeam("Beta", mavenWorkshop, List.of("s3"));

        LOG.info("Demo data ready: {} instructors, {} students ({} free), {} teams",
                system.getInstructors().size(), system.getStudents().size(),
                system.getFreeStudents().size(), system.getTeams().size());

        export(system.getClass().getSimpleName(), system);
    }

    private static void export(String systemName, WorkshopSystem system) {
        File instructors = new File("instructors.json");
        File students = new File("students.json");
        File teams = new File("teams.json");

        system.exportInstructorsDataToJson(instructors);
        system.exportStudentsDataToJson(students);
        system.exportTeamsDataToJson(teams);

        LOG.info("{} wrote {}, {} and {}", systemName,
                instructors.getAbsolutePath(), students.getAbsolutePath(), teams.getAbsolutePath());
    }
}