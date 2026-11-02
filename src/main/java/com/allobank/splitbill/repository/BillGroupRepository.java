package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.BillGroup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillGroupRepository extends JpaRepository<BillGroup, UUID> {

    List<BillGroup> findByOwnerSessionId(String ownerSessionId);

    Optional<BillGroup> findByIdAndOwnerSessionId(UUID id, String ownerSessionId);
}
