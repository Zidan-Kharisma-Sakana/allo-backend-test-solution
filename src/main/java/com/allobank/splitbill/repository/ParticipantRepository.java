package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.Participant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {

    List<Participant> findByBillGroupIdOrderByPositionAsc(UUID billGroupId);
}
