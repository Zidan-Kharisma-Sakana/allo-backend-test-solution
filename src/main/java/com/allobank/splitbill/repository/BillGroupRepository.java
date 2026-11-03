package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.BillGroup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillGroupRepository extends JpaRepository<BillGroup, UUID> {

    @org.springframework.data.jpa.repository.Query(value = "SELECT g.* FROM bill_groups g JOIN group_members m ON m.bill_group_id = g.id WHERE m.session_id = :sessionId ORDER BY g.created_at, g.id", nativeQuery = true)
    List<BillGroup> findAccessibleBySessionId(@org.springframework.data.repository.query.Param("sessionId") String sessionId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT g.* FROM bill_groups g JOIN group_members m ON m.bill_group_id = g.id WHERE g.id = :id AND m.session_id = :sessionId", nativeQuery = true)
    Optional<BillGroup> findAccessibleById(@org.springframework.data.repository.query.Param("id") UUID id, @org.springframework.data.repository.query.Param("sessionId") String sessionId);

    Optional<BillGroup> findByInviteCode(String inviteCode);

    List<BillGroup> findByOwnerSessionId(String ownerSessionId);

    Optional<BillGroup> findByIdAndOwnerSessionId(UUID id, String ownerSessionId);
}
