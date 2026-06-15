package com.library.config;

import com.library.entity.User;
import com.library.entity.enums.Role;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initializes default users when the application starts.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        createDefaultAdmin();
        createDefaultLibrarian();
    }

    private void createDefaultAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            log.info("Creating default admin account...");
            User admin = User.builder()
                    .username("admin")
                    .email("admin@library.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("Default admin account created. Username: admin, Password: admin123");
        }
    }

    private void createDefaultLibrarian() {
        if (!userRepository.existsByUsername("librarian")) {
            log.info("Creating default librarian account...");
            User librarian = User.builder()
                    .username("librarian")
                    .email("librarian@library.com")
                    .passwordHash(passwordEncoder.encode("librarian123"))
                    .fullName("System Librarian")
                    .role(Role.LIBRARIAN)
                    .active(true)
                    .build();
            userRepository.save(librarian);
            log.info("Default librarian account created. Username: librarian, Password: librarian123");
        }
    }
}
