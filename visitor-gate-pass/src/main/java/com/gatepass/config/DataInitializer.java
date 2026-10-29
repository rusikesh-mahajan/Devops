package com.gatepass.config;

import com.gatepass.entity.GroupVisit;
import com.gatepass.entity.User;
import com.gatepass.entity.VisitorPass;
import com.gatepass.repository.GroupVisitRepository;
import com.gatepass.repository.UserRepository;
import com.gatepass.repository.VisitorPassRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepo, 
                                     VisitorPassRepository passRepo, 
                                     GroupVisitRepository groupRepo,
                                     PasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() == 0) {
                User admin = userRepo.save(new User("admin", encoder.encode("admin123"), "ROLE_ADMIN", "System Administrator", "HQ-01", "+1 555-0100"));
                User guard = userRepo.save(new User("guard1", encoder.encode("guard123"), "ROLE_GUARD", "Front Gate Guard", "GATE-01", "+1 555-0101"));
                User resident1 = userRepo.save(new User("resident1", encoder.encode("res123"), "ROLE_RESIDENT", "John Doe", "B-204", "+1 555-0204"));
                User resident2 = userRepo.save(new User("resident2", encoder.encode("res123"), "ROLE_RESIDENT", "Jane Smith", "A-105", "+1 555-0105"));
                System.out.println("Default users seeded: admin, guard1, resident1, resident2.");

                if (passRepo.count() == 0) {
                    VisitorPass p1 = new VisitorPass();
                    p1.setPassCode("GP-1001");
                    p1.setVisitorName("Robert Johnson");
                    p1.setPhone("+1 555-123-4567");
                    p1.setPurpose("Plumbing Maintenance");
                    p1.setVehicleNo("TX-4421");
                    p1.setStatus("PENDING");
                    p1.setHost(resident1);
                    passRepo.save(p1);

                    VisitorPass p2 = new VisitorPass();
                    p2.setPassCode("GP-1002");
                    p2.setVisitorName("Sarah Miller");
                    p2.setPhone("+1 555-987-6543");
                    p2.setPurpose("Weekend Guest");
                    p2.setVehicleNo("CA-9988");
                    p2.setStatus("APPROVED");
                    p2.setHost(resident1);
                    passRepo.save(p2);

                    VisitorPass p3 = new VisitorPass();
                    p3.setPassCode("GP-1003");
                    p3.setVisitorName("David Clark");
                    p3.setPhone("+1 555-555-0143");
                    p3.setPurpose("Courier Delivery");
                    p3.setVehicleNo("NY-7711");
                    p3.setStatus("CHECKED_IN");
                    p3.setHost(resident2);
                    p3.setCheckInTime(LocalDateTime.now().minusMinutes(45));
                    passRepo.save(p3);

                    VisitorPass p4 = new VisitorPass();
                    p4.setPassCode("GP-1004");
                    p4.setVisitorName("Emily Davis");
                    p4.setPhone("+1 555-444-9988");
                    p4.setPurpose("Interior Consultation");
                    p4.setVehicleNo("FL-3322");
                    p4.setStatus("CHECKED_OUT");
                    p4.setHost(resident2);
                    p4.setCheckInTime(LocalDateTime.now().minusHours(2));
                    p4.setCheckOutTime(LocalDateTime.now().minusMinutes(30));
                    passRepo.save(p4);

                    System.out.println("Sample visitor passes seeded across all workflow states.");
                }

                if (groupRepo.count() == 0) {
                    // Seed Sample Group Visits
                    GroupVisit g1 = new GroupVisit();
                    g1.setGroupCode("GV-001");
                    g1.setPrimaryVisitorName("Rahul Sharma");
                    g1.setPhone("+1 555-123-0001");
                    g1.setVehicleNo("MH-12-RS-5050");
                    g1.setPurpose("Family Gathering & Dinner");
                    g1.setTotalGuests(5); // 5 Total: Rahul Sharma + 4 Accompanying
                    g1.setAccompanyingGuestNames("Sunil Sharma, Ananya Sharma, Rohit Verma, Priya Verma");
                    g1.setHost(resident1);
                    g1.setFlatNumber("B-204");
                    g1.setStatus("CHECKED_IN");
                    g1.setCheckInTime(LocalDateTime.now().minusMinutes(50));
                    groupRepo.save(g1);

                    GroupVisit g2 = new GroupVisit();
                    g2.setGroupCode("GV-002");
                    g2.setPrimaryVisitorName("Priya Patel");
                    g2.setPhone("+1 555-987-0002");
                    g2.setVehicleNo("DL-01-PP-1010");
                    g2.setPurpose("Weekend Family Visit");
                    g2.setTotalGuests(3); // 3 Total: Priya Patel + 2 Accompanying
                    g2.setAccompanyingGuestNames("Kiran Patel, Aarav Patel");
                    g2.setHost(resident2);
                    g2.setFlatNumber("A-105");
                    g2.setStatus("APPROVED");
                    groupRepo.save(g2);

                    System.out.println("Sample group visits seeded: GV-001 (5 guests checked in), GV-002 (3 guests approved).");
                }
            }
        };
    }
}
