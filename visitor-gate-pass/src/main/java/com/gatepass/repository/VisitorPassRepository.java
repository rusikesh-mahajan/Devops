package com.gatepass.repository;

import com.gatepass.entity.VisitorPass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VisitorPassRepository extends JpaRepository<VisitorPass, Long> {
    List<VisitorPass> findByHostId(Long hostId);
    List<VisitorPass> findByHostIdOrderByCreatedAtDesc(Long hostId);
    List<VisitorPass> findByStatus(String status);
    List<VisitorPass> findByStatusOrderByCreatedAtDesc(String status);
    List<VisitorPass> findAllByOrderByCreatedAtDesc();
    Optional<VisitorPass> findByPassCode(String passCode);

    @Query("SELECT v FROM VisitorPass v WHERE " +
           "LOWER(v.visitorName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.purpose) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.vehicleNo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.status) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.passCode) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY v.createdAt DESC")
    List<VisitorPass> searchAllFields(@Param("query") String query);

    @Query("SELECT v FROM VisitorPass v WHERE v.host.id = :hostId AND (" +
           "LOWER(v.visitorName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.purpose) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.status) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY v.createdAt DESC")
    List<VisitorPass> searchHostPasses(@Param("hostId") Long hostId, @Param("query") String query);
}
