package com.tracer.service;

import com.tracer.model.Student;
import com.tracer.model.Teacher;
import com.tracer.model.request.student.AddStudentRequest;
import com.tracer.model.request.student.EditStudentRequest;
import com.tracer.repository.StudentRepository;
import com.tracer.repository.TeacherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TeacherService implements UserDetailsService {
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private StudentRepository studentRepository;
    private final Logger logger = LoggerFactory.getLogger(TeacherService.class);

    /**
     * Uses the teachers unique username to find the list of corresponding students.
     * @param username Teacher unique username
     * @return a list of students matching the teacher.
     */
    public List<Student> getAllStudentsByTeacherUsername(String username) {
        logger.info(String.format("Getting all students associated with teacher: %s", username));
        Teacher teacher = teacherRepository.findByUsername(username)
                .orElseThrow(NullPointerException::new);
        return teacher.getStudents().parallelStream().toList();
    }

    /**
     * Gets a list of students based on the teachers username and the students full name.
     * Returns a list in the event there is more than 1 student with the same name.
     * @param teacherUsername Teachers username
     * @param studentName student's full name
     * @return A list of students matching studentName
     */
    public List<Student> getStudentsByName(String teacherUsername, String studentName) {
        logger.info(String.format("Getting student with name %s associated with %s.", studentName, teacherUsername));
        Teacher teacher = teacherRepository.findByUsername(teacherUsername)
                .orElseThrow(NullPointerException::new);

        return teacher.getStudents().parallelStream()
                .filter(student -> Objects.equals(student.getName(), studentName))
                .collect(Collectors.toList());
    }

    /**
     * Creates a new Student to add to an existing teacher.
     * @param request request body with all the information needed to create a student
     * @return An updated list of students
     */
    public List<Student> addStudent(AddStudentRequest request, String teacherUsername) {
        logger.info(String.format("beginning add student process for: %s.", teacherUsername));
        Teacher teacher = teacherRepository.findByUsername(teacherUsername)
                .orElseThrow(NullPointerException::new);
        Student studentToAdd = new Student(request.getName() , request.getPeriod(), BigDecimal.valueOf(100));
        Set<Student> students = teacher.getStudents();
        if (students.contains(studentToAdd)) throw new RuntimeException("Student Already Exist");
        students.add(studentToAdd);
        teacher.setStudents(students);
        teacherRepository.save(teacher);
        return students.parallelStream().toList();
    }

    /**
     * Edits an existing student's data
     * @param request request body with all information needed to handle the edit
     * @return an updated List of students
     */
    public List<Student> editExistingStudent(EditStudentRequest request, String teacherUsername) {
        Teacher teacher = teacherRepository.findByUsername(teacherUsername)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        logger.info(String.format("editing student with id: %d.", request.getStudentId()));
        List<Student> updatedStudents = teacher.getStudents().parallelStream()
                .peek(student -> {
                    if (student.getStudentId().equals(request.getStudentId())) {
                        if (!request.getNameToChange().isEmpty()) {
                            student.setName(request.getNameToChange());
                        }
                        request.getPeriodToChange().ifPresent(student::setPeriod);
                        studentRepository.save(student);
                    }
                })
                .collect(Collectors.toList());

        teacherRepository.save(teacher);

        return updatedStudents;
    }

    public void saveTeacher(Teacher teacher) {
        teacherRepository.save(teacher);
    }

    /**
     * Deletes student using a teacher username and student Id
     * @param studentId unique id given to student objects
     * @param teacherUsername unique username
     */
    public void deleteStudent(Long studentId, String teacherUsername) {
        Teacher teacher = teacherRepository.findByUsername(teacherUsername)
                .orElseThrow(NullPointerException::new);
        logger.info(String.format("deleting student with id: %d.", studentId));
        Optional<Student> studentToDelete = teacher.getStudents().parallelStream()
                        .filter(student -> student.getStudentId().equals(studentId))
                                .findFirst();
        studentToDelete.ifPresent(teacher.getStudents()::remove);
        studentToDelete.ifPresent(student -> {
            studentRepository.delete(student);
            teacherRepository.save(teacher);
        });
    }

    /**
     *  Fetches a User with a given username if the user is not found throws
     *  A UsernameNotFoundException.
     * @param username the username identifying the user whose data is required.
     * @return A UserDetails with a valid user
     * @throws UsernameNotFoundException If user was not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info(String.format("finding teacher with username: %s.", username));
        Optional<Teacher> teacherOpt = teacherRepository.findByUsername(username);
        return teacherOpt.orElseThrow(() -> new UsernameNotFoundException("Invalid Credentials"));
    }

    /**
     * Fetches a User with a given email if the user is not found throws a
     * UsernameNotFoundException.
     * @param email Users email address.
     * @return the user details matching the email.
     * @throws UsernameNotFoundException user was not found in the database.
     */
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        logger.info(String.format("finding teacher with email: %s", email));
        Optional<Teacher> teacherOpt = teacherRepository.findByEmail(email);
        return teacherOpt.orElseThrow(() -> new UsernameNotFoundException("Invalid Credentials"));
    }

    /**
     * Saves a new Teacher to the database.
     * @param teacher user to be saved in database.
     * @return the userDetails of that teacher.
     */
    public UserDetails saveNewTeacher(Teacher teacher) {
        logger.info("Saving new teacher.");
        return teacherRepository.save(teacher);
    }
}
