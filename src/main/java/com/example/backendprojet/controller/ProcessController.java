package com.example.backendprojet.controller;




import com.example.backendprojet.dto.ProcessusDTO;
import com.example.backendprojet.services.Processservice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProcessController {

    private final Processservice service;

    @GetMapping
    public List<ProcessusDTO> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public ProcessusDTO getOne(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ProcessusDTO create(@RequestBody ProcessusDTO dto) { return service.create(dto); }

    @PutMapping("/{id}")
    public ProcessusDTO update(@PathVariable Long id, @RequestBody ProcessusDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle")
    public ProcessusDTO toggle(@PathVariable Long id) { return service.toggleActive(id); }
}