package com.ticket.support_management_system_api.features.ticket.entities;

import com.ticket.support_management_system_api.common.entity.BaseEntity;
import com.ticket.support_management_system_api.features.ticket.enums.ETicketCommentType;
import com.ticket.support_management_system_api.features.user.entities.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "ticket_comments",
    indexes = {
        @Index(name = "idx_ticket_comments_ticket_id", columnList = "ticket_id"),
        @Index(name = "idx_ticket_comments_archived_at", columnList = "archived_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_type", nullable = false)
    private ETicketCommentType commentType;
}
