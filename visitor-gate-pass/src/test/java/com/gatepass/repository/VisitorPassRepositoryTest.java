package com.gatepass.repository;

import com.gatepass.entity.User;
import com.gatepass.entity.VisitorPass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class VisitorPassRepositoryTest {

    @Autowired
    private VisitorPassRepository passRepository;

    @Autowired
    private UserRepository userRepository;

    private User residentUser;

    @BeforeEach
    public void setup() {
        residentUser = userRepository.save(new User("repo_resident", "hashedpass", "ROLE_RESIDENT", "Test Resident"));
    }

    @Test
    @DisplayName("Should successfully persist visitor pass and auto-generate passCode and createdAt")
    public void testPersistVisitorPass() {
        VisitorPass pass = new VisitorPass();
        pass.setVisitorName("Michael Scott");
        pass.setPhone("+1 555-222-3333");
        pass.setPurpose("Business Meeting");
        pass.setStatus("PENDING");
        pass.setHost(residentUser);

        VisitorPass saved = passRepository.save(pass);

        Assertions.assertNotNull(saved.getId());
        Assertions.assertNotNull(saved.getPassCode());
        Assertions.assertTrue(saved.getPassCode().startsWith("GP-"));
        Assertions.assertNotNull(saved.getCreatedAt());
        Assertions.assertEquals("PENDING", saved.getStatus());
    }

    @Test
    @DisplayName("Should find passes by status")
    public void testFindByStatus() {
        VisitorPass p1 = new VisitorPass();
        p1.setVisitorName("Jim Halpert");
        p1.setPhone("+1 555-111-2222");
        p1.setPurpose("Sales Pitch");
        p1.setStatus("APPROVED");
        p1.setHost(residentUser);
        passRepository.save(p1);

        VisitorPass p2 = new VisitorPass();
        p2.setVisitorName("Pam Beesly");
        p2.setPhone("+1 555-333-4444");
        p2.setPurpose("Design Review");
        p2.setStatus("PENDING");
        p2.setHost(residentUser);
        passRepository.save(p2);

        List<VisitorPass> approved = passRepository.findByStatus("APPROVED");
        Assertions.assertFalse(approved.isEmpty());
        boolean hasJim = approved.stream().anyMatch(p -> "Jim Halpert".equals(p.getVisitorName()));
        Assertions.assertTrue(hasJim);
    }

    @Test
    @DisplayName("Should find passes by Host ID")
    public void testFindByHostId() {
        VisitorPass pass = new VisitorPass();
        pass.setVisitorName("Dwight Schrute");
        pass.setPhone("+1 555-777-8888");
        pass.setPurpose("Beet Delivery");
        pass.setStatus("PENDING");
        pass.setHost(residentUser);
        passRepository.save(pass);

        List<VisitorPass> hostPasses = passRepository.findByHostId(residentUser.getId());
        Assertions.assertFalse(hostPasses.isEmpty());
        Assertions.assertEquals("Dwight Schrute", hostPasses.get(0).getVisitorName());
    }

    @Test
    @DisplayName("Should search across all fields (name, purpose, vehicle, passCode)")
    public void testSearchAllFields() {
        VisitorPass pass = new VisitorPass();
        pass.setVisitorName("Stanley Hudson");
        pass.setPhone("+1 555-999-0000");
        pass.setPurpose("Pretzel Delivery");
        pass.setVehicleNo("SCRANTON-1");
        pass.setStatus("PENDING");
        pass.setHost(residentUser);
        passRepository.save(pass);

        List<VisitorPass> byName = passRepository.searchAllFields("Stanley");
        Assertions.assertFalse(byName.isEmpty());

        List<VisitorPass> byVehicle = passRepository.searchAllFields("SCRANTON");
        Assertions.assertFalse(byVehicle.isEmpty());

        List<VisitorPass> byPurpose = passRepository.searchAllFields("Pretzel");
        Assertions.assertFalse(byPurpose.isEmpty());
    }
}
