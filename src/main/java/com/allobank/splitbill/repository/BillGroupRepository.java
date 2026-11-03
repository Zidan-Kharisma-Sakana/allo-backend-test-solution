package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.BillGroup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillGroupRepository extends JpaRepository<BillGroup, UUID> {

    @Query(value = """
            SELECT g.* FROM bill_groups g
            JOIN group_members m ON m.bill_group_id = g.id
            WHERE m.session_id = :sessionId
            ORDER BY g.created_at, g.id
            """, nativeQuery = true)
    List<BillGroup> findAccessibleBySessionId(@Param("sessionId") String sessionId);

    @Query(value = """
            SELECT g.* FROM bill_groups g
            JOIN group_members m ON m.bill_group_id = g.id
            WHERE g.id = :id AND m.session_id = :sessionId
            """, nativeQuery = true)
    Optional<BillGroup> findAccessibleById(@Param("id") UUID id, @Param("sessionId") String sessionId);

    Optional<BillGroup> findByInviteCode(String inviteCode);
}
