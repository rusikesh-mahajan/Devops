package com.gatepass.repository;

import com.gatepass.entity.GroupVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupVisitRepository extends JpaRepository<GroupVisit, Long> {

    Optional<GroupVisit> findByGroupCode(String groupCode);

    List<GroupVisit> findByStatus(String status);

    List<GroupVisit> findByStatusOrderByCreatedAtDesc(String status);

    List<GroupVisit> findByHostId(Long hostId);

    List<GroupVisit> findByHostIdOrderByCreatedAtDesc(Long hostId);

    List<GroupVisit> findAllByOrderByCreatedAtDesc();

    long countByStatus(String status);

    // Dynamic metrics
    @Query("SELECT COALESCE(SUM(g.totalGuests), 0) FROM GroupVisit g WHERE g.status = 'CHECKED_IN'")
    long sumActiveCheckedInGuests();

    @Query("SELECT COALESCE(SUM(g.totalGuests), 0) FROM GroupVisit g WHERE g.status = 'APPROVED'")
    long sumExpectedGuests();

    @Query("SELECT COALESCE(SUM(g.totalGuests), 0) FROM GroupVisit g WHERE g.checkInTime >= :startOfDay")
    long sumCheckedInGuestsToday(@Param("startOfDay") LocalDateTime startOfDay);

    // Full-text search across all relevant fields
    @Query("SELECT g FROM GroupVisit g WHERE " +
           "LOWER(g.groupCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.primaryVisitorName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.phone) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.purpose) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.vehicleNo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.flatNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.status) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(g.accompanyingGuestNames IS NOT NULL AND LOWER(g.accompanyingGuestNames) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
           "LOWER(g.host.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY g.createdAt DESC")
    List<GroupVisit> searchAllGroupVisits(@Param("query") String query);

    @Query("SELECT g FROM GroupVisit g WHERE g.host.id = :hostId AND (" +
           "LOWER(g.groupCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.primaryVisitorName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.purpose) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.status) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(g.accompanyingGuestNames IS NOT NULL AND LOWER(g.accompanyingGuestNames) LIKE LOWER(CONCAT('%', :query, '%')))) " +
           "ORDER BY g.createdAt DESC")
    List<GroupVisit> searchHostGroupVisits(@Param("hostId") Long hostId, @Param("query") String query);
}

