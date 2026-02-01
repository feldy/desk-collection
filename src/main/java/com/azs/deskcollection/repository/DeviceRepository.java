package com.azs.deskcollection.repository;

import com.azs.deskcollection.model.Device;
import com.azs.deskcollection.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByUser(User user);

    Optional<Device> findBySessionName(String sessionName);
}
