package com.ticket.support_management_system_api.features.user.repository;

import com.ticket.support_management_system_api.features.user.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Optional<User> findByEmail(String email);

    @Query("SELECT ed.user.id FROM ExternalDetails ed WHERE ed.userType.allTicketAccess = true AND ed.userType.allProjectAccess = true AND ed.user.archivedAt IS NULL")
    List<UUID> findAdminUserIds();

    @Query("""
            SELECT u FROM User u
            WHERE u.archivedAt IS NULL
              AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY u.createdAt DESC
            """)
    List<User> searchByName(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE u.archivedAt IS NULL
              AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (
                    u.id IN (SELECT cd.user.id FROM CustomerDetails cd WHERE cd.company.id = :companyId)
                 OR u.id IN (SELECT ta.user.id FROM TicketAssignee ta WHERE ta.archivedAt IS NULL AND ta.ticket.project.company.id = :companyId)
              )
            ORDER BY u.createdAt DESC
            """)
    List<User> searchByNameAndCompanyId(@Param("keyword") String keyword,
                                         @Param("companyId") UUID companyId,
                                         Pageable pageable);
}
