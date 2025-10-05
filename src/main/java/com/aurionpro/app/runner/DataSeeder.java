package com.aurionpro.app.runner;

import com.aurionpro.app.entity.*;
import com.aurionpro.app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;
    private final FareRuleRepository fareRuleRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Executing DataSeeder...");
        seedRoles();
        seedUsers();
        seedNetwork();
        seedFareRules();
        log.info("DataSeeder finished.");
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Created role: {}", roleName);
            }
        }
    }

    private void seedUsers() {
        if (userRepository.findByEmail("admin@metro.com").isEmpty()) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("FATAL: ROLE_ADMIN not found"));
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("FATAL: ROLE_USER not found"));

            User adminUser = User.builder()
                    .email("admin@metro.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .roles(Set.of(adminRole, userRole))
                    .build();
            userRepository.save(adminUser);
            log.info("Created default admin user.");
        }
        
        if (userRepository.findByEmail("staff@metro.com").isEmpty()) {
            Role staffRole = roleRepository.findByName(RoleName.ROLE_STAFF)
                    .orElseThrow(() -> new RuntimeException("FATAL: ROLE_STAFF not found"));

            User staffUser = User.builder()
                    .email("staff@metro.com")
                    .password(passwordEncoder.encode("Staff@123"))
                    .roles(Set.of(staffRole))
                    .build();
            userRepository.save(staffUser);
            log.info("Created default staff user.");
        }
    }

    private void seedNetwork() {
        if (stationRepository.count() == 0) {
            log.info("No stations found. Seeding Mumbai Metro Blue Line 1...");
            List<Station> blueLineStations = List.of(
                Station.builder().name("Versova").code("VER").stationOrder(1).build(),
                Station.builder().name("D. N. Nagar").code("DNN").stationOrder(2).build(),
                Station.builder().name("Azad Nagar").code("AZN").stationOrder(3).build(),
                Station.builder().name("Andheri").code("AND").stationOrder(4).build(),
                Station.builder().name("Western Express Highway").code("WEH").stationOrder(5).build(),
                Station.builder().name("Chakala (J.B. Nagar)").code("CHK").stationOrder(6).build(),
                Station.builder().name("Airport Road").code("AIR").stationOrder(7).build(),
                Station.builder().name("Marol Naka").code("MRN").stationOrder(8).build(),
                Station.builder().name("Saki Naka").code("SKN").stationOrder(9).build(),
                Station.builder().name("Asalpha").code("ASL").stationOrder(10).build(),
                Station.builder().name("Jagruti Nagar").code("JGN").stationOrder(11).build(),
                Station.builder().name("Ghatkopar").code("GHT").stationOrder(12).build()
            );
            List<Station> savedStations = stationRepository.saveAll(blueLineStations);
            log.info("Seeded {} stations.", savedStations.size());

            if (routeRepository.count() == 0) {
                Route blueLine = Route.builder().name("Blue Line 1").build();
                blueLine.setStations(savedStations);
                routeRepository.save(blueLine);
                log.info("Created route '{}' and linked {} stations.", blueLine.getName(), savedStations.size());
            }
        }
    }

    private void seedFareRules() {
        if (fareRuleRepository.count() == 0) {
            log.info("No fare rules found. Seeding default pricing model...");
            List<FareRule> rules = List.of(
                // --- SINGLE Journey Rules ---
                FareRule.builder().ticketType(TicketType.SINGLE).minStationCount(1).maxStationCount(3).fare(new BigDecimal("10.00")).totalTrips(1).build(),
                FareRule.builder().ticketType(TicketType.SINGLE).minStationCount(4).maxStationCount(6).fare(new BigDecimal("20.00")).totalTrips(1).build(),
                FareRule.builder().ticketType(TicketType.SINGLE).minStationCount(7).maxStationCount(9).fare(new BigDecimal("30.00")).totalTrips(1).build(),
                FareRule.builder().ticketType(TicketType.SINGLE).minStationCount(10).maxStationCount(11).fare(new BigDecimal("40.00")).totalTrips(1).build(),

                // --- RETURN Journey Rules ---
                FareRule.builder().ticketType(TicketType.RETURN).minStationCount(1).maxStationCount(3).fare(new BigDecimal("18.00")).totalTrips(2).build(),
                FareRule.builder().ticketType(TicketType.RETURN).minStationCount(4).maxStationCount(6).fare(new BigDecimal("35.00")).totalTrips(2).build(),
                FareRule.builder().ticketType(TicketType.RETURN).minStationCount(7).maxStationCount(9).fare(new BigDecimal("55.00")).totalTrips(2).build(),
                FareRule.builder().ticketType(TicketType.RETURN).minStationCount(10).maxStationCount(11).fare(new BigDecimal("75.00")).totalTrips(2).build(),
                
                // --- Pass Rules ---
                FareRule.builder().ticketType(TicketType.DAY_PASS).durationInDays(1).totalTrips(null).fare(new BigDecimal("150.00")).build(),
                FareRule.builder().ticketType(TicketType.MONTHLY_PASS).durationInDays(30).totalTrips(45).fare(new BigDecimal("800.00")).build()
            );
            fareRuleRepository.saveAll(rules);
            log.info("Seeded {} fare rules.", rules.size());
        }
    }
}