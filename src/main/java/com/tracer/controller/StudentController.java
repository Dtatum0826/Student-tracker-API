package com.tracer.controller;

import com.tracer.model.Student;
import com.tracer.model.assignments.Assignment;
import com.tracer.model.request.assignment.AddAssignmentRequest;
import com.tracer.model.request.assignment.DeleteAssignmentRequest;
import com.tracer.model.request.assignment.EditAssignmentRequest;
import com.tracer.model.request.assignment.UpdateAssignmentStatusRequest;
import com.tracer.service.StudentService;
import com.tracer.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
@CrossOrigin("*")
public class StudentController {
    @Autowired
    private StudentService service;
    @Autowired
    private TokenService tokenService;

    @GetMapping("/get-student{id}")
    public ResponseEntity<Student> getStudentById(@RequestParam Long id) {
        return new ResponseEntity<Student>(service.getStudentById(id), HttpStatus.OK);
    }
    @GetMapping("/get-all{id}")
    public ResponseEntity<List<Assignment>> getAllAssignmentsByStudentId(@RequestParam Long id,
                                                                         HttpServletRequest servletRequest, Authentication authentication ) {
        tokenService.validateJwt(servletRequest, authentication);
        return new ResponseEntity<List<Assignment>>(
                service.getAllAssignmentsByStudentId(id), HttpStatus.OK
        );
    }

    @PutMapping("/add")
    public ResponseEntity<List<Assignment>> addNewAssignment(@RequestBody AddAssignmentRequest request,
                                                             HttpServletRequest servletRequest, Authentication authentication ) {
        tokenService.validateJwt(servletRequest, authentication);
        return new ResponseEntity<List<Assignment>>(
                service.addNewAssignment(request), HttpStatus.CREATED
        );
    }

    @PostMapping("/update-status/")
    public ResponseEntity<List<Assignment>> updateStatus(@RequestBody UpdateAssignmentStatusRequest request,
                                                         HttpServletRequest servletRequest, Authentication authentication ) {
        tokenService.validateJwt(servletRequest, authentication);
        return new ResponseEntity<>(
                service.updateCompleteStatus(request.getStudentId(), request.getAssignmentId()), HttpStatus.OK
        );
    }

    @PostMapping("/edit")
    public ResponseEntity<List<Assignment>> editAssignment(@RequestBody EditAssignmentRequest request,
                                                           HttpServletRequest servletRequest, Authentication authentication ) {
        tokenService.validateJwt(servletRequest, authentication);
        return new ResponseEntity<List<Assignment>>(
                service.editAssignment(request), HttpStatus.OK
        );
    }

    @DeleteMapping("/delete")
    public ResponseEntity<List<Assignment>> deleteAssignment(@RequestBody DeleteAssignmentRequest request,
                                                             HttpServletRequest servletRequest, Authentication authentication ) {
        tokenService.validateJwt(servletRequest, authentication);
        return new ResponseEntity<List<Assignment>>(
                service.deleteAssignment(request), HttpStatus.OK
        );
    }
}
