package com.azs.deskcollection.controller;

import com.azs.deskcollection.model.Desk;
import com.azs.deskcollection.service.DeskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/desks")
public class DeskController {

    private final DeskService deskService;

    @Autowired
    public DeskController(DeskService deskService) {
        this.deskService = deskService;
    }

    @GetMapping
    public List<Desk> getAllDesks() {
        return deskService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Desk> getDesk(@PathVariable Long id) {
        return deskService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Desk createDesk(@RequestBody Desk desk) {
        return deskService.save(desk);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Desk> updateDesk(@PathVariable Long id, @RequestBody Desk deskDetails) {
        return deskService.findById(id)
                .map(desk -> {
                    desk.setName(deskDetails.getName());
                    desk.setDescription(deskDetails.getDescription());
                    desk.setDimensions(deskDetails.getDimensions());
                    desk.setMaterial(deskDetails.getMaterial());
                    desk.setPrice(deskDetails.getPrice());
                    desk.setImageUrl(deskDetails.getImageUrl());
                    return ResponseEntity.ok(deskService.save(desk));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesk(@PathVariable Long id) {
        deskService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
