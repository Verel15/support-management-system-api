package com.ticket.support_management_system_api.config;

import com.ticket.support_management_system_api.common.enums.AccountType;
import com.ticket.support_management_system_api.common.enums.CommonStatus;
import com.ticket.support_management_system_api.features.user.entities.ExternalDetails;
import com.ticket.support_management_system_api.features.user.entities.User;
import com.ticket.support_management_system_api.features.user.repository.ExternalDetailsRepository;
import com.ticket.support_management_system_api.features.user.repository.UserRepository;
import com.ticket.support_management_system_api.features.user_type.repository.UserTypeRepository;
import com.ticket.support_management_system_api.features.user_type.entities.UserType;
import com.ticket.support_management_system_api.features.user_type.enums.MyTicketAccess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final ExternalDetailsRepository externalDetailsRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.init.admin-email:admin@example.com}")
    private String adminEmail;

    @Value("${application.init.admin-password:Admin@1234}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("No users found — seeding initial admin user...");

        UserType adminType = userTypeRepository.findByName("Superadmin").orElseGet(() ->
                userTypeRepository.save(UserType.builder()
                        .name("Superadmin")
                        .myTicketAccess(MyTicketAccess.ADMIN)
                        .allProjectAccess(true)
                        .notificationAccess(true)
                        .dashboardAccess(true)
                        .allTicketAccess(true)
                        .manageProjectAccess(true)
                        .manageUserAccess(true)
                        .manageCompanyAccess(true)
                        .manageDataAccess(true)
                        .build())
        );

        User admin = userRepository.save(User.builder()
                .accountType(AccountType.EXTERNAL)
                .firstName("Super")
                .lastName("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .status(CommonStatus.ACTIVE)
                .build());

        externalDetailsRepository.save(ExternalDetails.builder()
                .user(admin)
                .userType(adminType)
                .build());

        log.info("Admin user created — email: {}", adminEmail);
    }
}
