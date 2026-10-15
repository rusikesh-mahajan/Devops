package com.gatepass.config;

import com.gatepass.entity.User;
import com.gatepass.entity.VisitorPass;
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
    public CommandLineRunner initData(UserRepository userRepo, VisitorPassRepository passRepo, PasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() == 0) {
                User admin = userRepo.save(new User("admin", encoder.encode("admin123"), "ROLE_ADMIN", "System Administrator"));
                User guard = userRepo.save(new User("guard1", encoder.encode("guard123"), "ROLE_GUARD", "Front Gate Guard"));
                User resident1 = userRepo.save(new User("resident1", encoder.encode("res123"), "ROLE_RESIDENT", "John Doe (Apt 101)"));
                User resident2 = userRepo.save(new User("resident2", encoder.encode("res123"), "ROLE_RESIDENT", "Jane Smith (Apt 102)"));
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
            }
        };
    }
}
