package com.university.resultsystem.repository;

import com.university.resultsystem.model.Lecturer;
import com.university.resultsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, UUID> {
    Optional<Lecturer> findByStaffId(String staffId);

    Optional<Lecturer> findByUser(User user);
}
