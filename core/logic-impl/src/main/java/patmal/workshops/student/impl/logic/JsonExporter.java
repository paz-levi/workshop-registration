package patmal.workshops.student.impl.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Person;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the JSON documents of the three exports with Jackson's tree model.
 * Every document is a tree: an object never points back to one of its ancestors.
 */
class JsonExporter {

    private final ObjectMapper mapper = new ObjectMapper();

    /** Instructors, each with its workshops, each workshop with its teams and their students. */
    void exportInstructors(List<Instructor> instructors, List<Team> teams, File target) {
        ArrayNode root = mapper.createArrayNode();
        for (Instructor instructor : instructors) {
            ObjectNode instructorNode = personDetails(instructor);
            ArrayNode workshopsNode = instructorNode.putArray("workshops");
            for (Workshop workshop : instructor.getWorkshops()) {
                ObjectNode workshopNode = workshopDetails(workshop);
                ArrayNode teamsNode = workshopNode.putArray("teams");
                for (Team team : teams) {
                    if (team.getWorkshop() == workshop) {
                        ObjectNode teamNode = teamDetails(team);
                        ArrayNode studentsNode = teamNode.putArray("students");
                        for (Student student : team.getStudents()) {
                            studentsNode.add(personDetails(student));
                        }
                        teamsNode.add(teamNode);
                    }
                }
                workshopsNode.add(workshopNode);
            }
            root.add(instructorNode);
        }
        write(root, target);
    }

    /** Every student (free or not) with its team, the team's workshop and the workshop's instructor. */
    void exportStudents(List<Student> students, List<Team> teams, File target) {
        // The Student interface has no getTeam(), so we find the team through the teams' member lists.
        Map<String, Team> teamOfStudent = new HashMap<>();
        for (Team team : teams) {
            for (Student student : team.getStudents()) {
                teamOfStudent.put(student.getId(), team);
            }
        }

        ArrayNode root = mapper.createArrayNode();
        for (Student student : students) {
            ObjectNode studentNode = personDetails(student);
            Team team = teamOfStudent.get(student.getId());
            if (team == null) {
                studentNode.putNull("team");
            } else {
                ObjectNode teamNode = teamDetails(team);
                ArrayNode idsNode = teamNode.putArray("studentIds");
                for (Student member : team.getStudents()) {
                    idsNode.add(member.getId());
                }
                teamNode.set("workshop", workshopWithInstructor(team.getWorkshop()));
                studentNode.set("team", teamNode);
            }
            root.add(studentNode);
        }
        write(root, target);
    }

    /** Every team with its students, its workshop and the workshop's instructor. */
    void exportTeams(List<Team> teams, File target) {
        ArrayNode root = mapper.createArrayNode();
        for (Team team : teams) {
            ObjectNode teamNode = teamDetails(team);
            ArrayNode studentsNode = teamNode.putArray("students");
            for (Student student : team.getStudents()) {
                studentsNode.add(personDetails(student));
            }
            teamNode.set("workshop", workshopWithInstructor(team.getWorkshop()));
            root.add(teamNode);
        }
        write(root, target);
    }

    /** id, name, email */
    private ObjectNode personDetails(Person person) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", person.getId());
        node.put("name", person.getName());
        node.put("email", person.getEmail());
        return node;
    }

    /** id, name, description */
    private ObjectNode workshopDetails(Workshop workshop) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", workshop.getId());
        node.put("name", workshop.getName());
        node.put("description", workshop.getDescription());
        return node;
    }

    /** id, name, description, instructor (or null when the workshop has no instructor) */
    private ObjectNode workshopWithInstructor(Workshop workshop) {
        ObjectNode node = workshopDetails(workshop);
        Instructor instructor = workshop.getInstructor();
        if (instructor == null) {
            node.putNull("instructor");
        } else {
            node.set("instructor", personDetails(instructor));
        }
        return node;
    }

    /** id, name */
    private ObjectNode teamDetails(Team team) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", team.getId());
        node.put("name", team.getName());
        return node;
    }

    private void write(ArrayNode root, File target) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(target, root);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}