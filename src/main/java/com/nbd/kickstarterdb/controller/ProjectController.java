package com.nbd.kickstarterdb.controller;

import com.nbd.kickstarterdb.model.Project;
import com.nbd.kickstarterdb.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class ProjectController {

    @Autowired
    ProjectRepository projectRepository;

    @GetMapping("/allProjects")
    public ResponseEntity<List<Project>> getAllProjects(@RequestParam(required = false, defaultValue = "0") Integer pageNum,
                                                        @RequestParam(required = false, defaultValue = "10") Integer pageSize) {

        try {
            Slice<Project> results = projectRepository.findAll(CassandraPageRequest.first(pageSize));
            for (int i = 0; i < pageNum; i++) {
                if (!results.hasNext()) return new ResponseEntity<>(null, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
                results = projectRepository.findAll(results.nextPageable());
            }

            List<Project> projects = results.getContent();
            return new ResponseEntity<>(projects, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    @GetMapping("project")
    public ResponseEntity<List<Project>> getProject(@RequestParam(required = false) List<Integer> ID, @RequestParam(required = false) List<String> name) {
        //if (ID != null && null != name) throw new RuntimeException("Too many parameters");
        if (ID == null && null == name) throw new RuntimeException("Either name or ID should be passed");

        List<Project> projects = new ArrayList<>();
        if (ID != null) projects.addAll(projectRepository.findAllById(ID));
        if (name != null) {
            for (String _name : name) {
                Optional<Project> p = projectRepository.findByName(_name);

                if (p.isPresent()) {
                    if (!projects.contains(p.get())) projects.add(p.get());
                }
            }
        }

        if (!projects.isEmpty()) {
            return new ResponseEntity<>(projects, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    // todo ether this should accept Integer or List and handle accordingly
    @GetMapping("/project/{ID}")
    public ResponseEntity<List<Project>> getProjectByIDPath(@PathVariable("ID") List<Integer> ID) {
//        List<Integer> temp = new ArrayList<>();
//        temp.add(ID);
        return getProject(ID, null);
    }


    @DeleteMapping("/allProjects")
    public ResponseEntity<Project> deleteProjects() {
        try {
            projectRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("project")
    public ResponseEntity<Project> deleteProjectByID(@RequestParam Integer ID) {
        try {
            projectRepository.deleteById(ID);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/project/{ID}")
    public ResponseEntity<Project> deleteProjectByIDPath(@PathVariable("ID") Integer ID) {
        return deleteProjectByID(ID);
    }


    @PostMapping("/project")
    public ResponseEntity<Project> createProject(@RequestBody Project project, @RequestParam(required = false) Integer ID) {
        try {
            if (ID != null) {
                project.setID(ID);
            }
            Project _project = projectRepository.save(new Project(project));
            return new ResponseEntity<>(_project, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/project/{ID}")
    public ResponseEntity<Project> createProjectPath(@RequestBody Project project, @PathVariable("ID") Integer ID) {
        return createProject(project, ID);
    }

    @PutMapping("/project")
    public ResponseEntity<Project> updateProject(@RequestBody Project project, @RequestParam(required = false) Integer ID) {
        Optional<Project> projectData;
        if (ID != null) {
            projectData = projectRepository.findById(ID);
        } else {
            projectData = projectRepository.findById(project.getID());
        }

        if (projectData.isPresent()) {
            Project _project = projectData.get();
            _project.setName(project.getName());
            _project.setCategory(project.getCategory());
            _project.setMain_category(project.getMain_category());
            _project.setCurrency(project.getCurrency());
            _project.setDeadline(project.getDeadline());
            _project.setGoal(project.getGoal());
            _project.setLaunched(project.getLaunched());
            _project.setPledged(project.getPledged());
            _project.setState(project.getState());
            _project.setBackers(project.getBackers());
            _project.setCountry(project.getCountry());
            _project.setUsd_pledged(project.getUsd_pledged());
            _project.setUsd_pledged_real(project.getUsd_pledged_real());
            _project.setUsd_goal_real(project.getUsd_goal_real());
            System.out.println(project);
            return new ResponseEntity<>(projectRepository.save(_project), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/project/{ID}")
    public ResponseEntity<Project> updateProjectPath(@RequestBody Project project, @PathVariable("ID") Integer ID) {
        return updateProject(project, ID);
    }

}
