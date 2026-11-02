package com.allobank.splitbill.repository;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GroupMemberRepository {

    private final JdbcTemplate jdbc;

    public void add(UUID groupId, String sessionId) {
        jdbc.update("INSERT INTO group_members (bill_group_id, session_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                groupId, sessionId);
    }
}
