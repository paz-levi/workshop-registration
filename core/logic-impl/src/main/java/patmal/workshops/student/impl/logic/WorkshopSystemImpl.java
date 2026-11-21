package patmal.workshops.student.impl.logic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import patmal.workshops.api.logic.WorkshopSystem;
import patmal.workshops.api.model.Instructor;
import patmal.workshops.api.model.Student;
import patmal.workshops.api.model.Team;
import patmal.workshops.api.model.Workshop;
import patmal.workshops.student.impl.model.InstructorImpl;
import patmal.workshops.student.impl.model.StudentImpl;
import patmal.workshops.student.impl.model.TeamImpl;
import patmal.workshops.student.impl.model.WorkshopImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Unknown keys: getInstructor, getStudent, getWorkshop and getTeam return null
 * for an unknown key; getStudentsOfInstructor and getStudentsOfWorkshop return
 * an empty list for an unknown id.
 */
public class WorkshopSystemImpl implements WorkshopSystem {

    private static final Logger LOG = LogManager.getLogger(WorkshopSystemImpl.class);

    private final LinkedHashMap<String, InstructorImpl> instructors = new LinkedHashMap<>();
    private final LinkedHashMap<String, StudentImpl> students = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, WorkshopImpl> workshops = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, TeamImpl> teams = new LinkedHashMap<>();

    private int nextWorkshopId = 1;
    private int nextTeamId = 1;

    @Override
    public Instructor createInstructor(String id, String name, String email) {
        if (instructors.containsKey(id)) {
            throw reject("Instructor already exists: " + id);
        }
        InstructorImpl instructor = new InstructorImpl(id, name, email);
        instructors.put(id, instructor);
        LOG.info("Created instructor {} ({})", id, name);
        return instructor;
    }

    @Override
    public Workshop createWorkshop(String name, String description) {
        int id = nextWorkshopId++;
        WorkshopImpl workshop = new WorkshopImpl(id, name, description);
        workshops.put(id, workshop);
        LOG.info("Created workshop {} ({})", id, name);
        return workshop;
    }

    @Override
    public void associateWorkshopWithInstructor(Instructor instructor, Workshop workshop) {
        if (instructor == null || workshop == null) {
            throw reject("Instructor and workshop must not be null");
        }
        if (instructors.get(instructor.getId()) != instructor) {
            throw reject("Instructor is not registered");
        }
        if (workshops.get(workshop.getId()) != workshop) {
            throw reject("Workshop is not registered");
        }
        InstructorImpl instructorImpl = (InstructorImpl) instructor;
        WorkshopImpl workshopImpl = (WorkshopImpl) workshop;
        Instructor current = workshopImpl.getInstructor();
        if (current == instructorImpl) {
            return;
        }
        if (current != null) {
            throw reject("Workshop already has a different instructor");
        }
        workshopImpl.setInstructor(instructorImpl);
        instructorImpl.addWorkshop(workshopImpl);
        LOG.info("Associated workshop {} with instructor {}", workshop.getId(), instructor.getId());
    }

    @Override
    public Student createStudent(String id, String name, String email) {
        if (students.containsKey(id)) {
            throw reject("Student already exists: " + id);
        }
        StudentImpl student = new StudentImpl(id, name, email);
        students.put(id, student);
        LOG.info("Created student {} ({})", id, name);
        return student;
    }

    @Override
    public Team createTeam(String name, Workshop workshop, List<String> memberIds) {
        if (workshop == null || workshops.get(workshop.getId()) != workshop) {
            throw reject("Workshop is not registered");
        }
        if (memberIds == null) {
            throw reject("memberIds must not be null");
        }
        Set<String> seen = new HashSet<>();
        List<StudentImpl> members = new ArrayList<>();
        for (String memberId : memberIds) {
            if (!seen.add(memberId)) {
                throw reject("Duplicate student id: " + memberId);
            }
            StudentImpl student = students.get(memberId);
            if (student == null) {
                throw reject("Unknown student id: " + memberId);
            }
            if (student.getTeamName() != null) {
                throw reject("Student already in a team: " + memberId);
            }
            members.add(student);
        }

        int id = nextTeamId++;
        TeamImpl team = new TeamImpl(id, name, workshop);
        for (StudentImpl student : members) {
            team.addStudent(student);
            student.setTeam(team);
        }
        teams.put(id, team);
        LOG.info("Created team {} ({}) in workshop {} with {} students", id, name, workshop.getId(), members.size());
        return team;
    }

    @Override
    public List<Team> getTeams() {
        return new ArrayList<>(teams.values());
    }

    @Override
    public Team getTeam(Integer id) {
        return teams.get(id);
    }

    @Override
    public List<Student> getStudents() {
        return new ArrayList<>(students.values());
    }

    @Override
    public Student getStudent(String id) {
        return students.get(id);
    }

    @Override
    public List<Workshop> getWorkshops() {
        return new ArrayList<>(workshops.values());
    }

    @Override
    public Workshop getWorkshop(Integer id) {
        return workshops.get(id);
    }

    @Override
    public List<Instructor> getInstructors() {
        return new ArrayList<>(instructors.values());
    }

    @Override
    public Instructor getInstructor(String id) {
        return instructors.get(id);
    }

    @Override
    public List<Student> getFreeStudents() {
        List<Student> result = new ArrayList<>();
        for (StudentImpl student : students.values()) {
            if (student.getTeamName() == null) {
                result.add(student);
            }
        }
        return result;
    }

    @Override
    public List<Student> getStudentsOfInstructor(String instructorId) {
        List<Student> result = new ArrayList<>();
        InstructorImpl instructor = instructors.get(instructorId);
        if (instructor == null) {
            return result;
        }
        for (StudentImpl student : students.values()) {
            if (student.getInstructor() == instructor) {
                result.add(student);
            }
        }
        return result;
    }

    @Override
    public List<Student> getStudentsOfWorkshop(Integer workshopId) {
        List<Student> result = new ArrayList<>();
        WorkshopImpl workshop = workshops.get(workshopId);
        if (workshop == null) {
            return result;
        }
        for (TeamImpl team : teams.values()) {
            if (team.getWorkshop() == workshop) {
                result.addAll(team.getStudents());
            }
        }
        return result;
    }

    /** Logs a rejected operation at warn level and returns the exception to throw. */
    private IllegalArgumentException reject(String message) {
        LOG.warn(message);
        return new IllegalArgumentException(message);
    }

    // TODO: temporary stubs; to be implemented with a JSON library (section 4.3 of the exercise).

    @Override
    public void exportInstructorsDataToJson(File targetFileName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void exportStudentsDataToJson(File targetFileName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void exportTeamsDataToJson(File targetFileName) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}