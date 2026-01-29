package com.azs.deskcollection.service;

import com.azs.deskcollection.model.Desk;
import com.azs.deskcollection.repository.DeskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeskService {

    private final DeskRepository deskRepository;

    @Autowired
    public DeskService(DeskRepository deskRepository) {
        this.deskRepository = deskRepository;
    }

    public List<Desk> findAll() {
        return deskRepository.findAll();
    }

    public Optional<Desk> findById(Long id) {
        return deskRepository.findById(id);
    }

    public Desk save(Desk desk) {
        return deskRepository.save(desk);
    }

    public void deleteById(Long id) {
        deskRepository.deleteById(id);
    }
}
