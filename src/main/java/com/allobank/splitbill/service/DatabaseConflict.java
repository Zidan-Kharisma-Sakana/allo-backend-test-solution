package com.allobank.splitbill.service;

import java.sql.SQLException;
import org.springframework.core.NestedExceptionUtils;

public final class DatabaseConflict {
    private DatabaseConflict() {}

    public static boolean isConstraint(Throwable failure, String name) {
        Throwable cause = NestedExceptionUtils.getMostSpecificCause(failure);
        return cause instanceof SQLException sql && "23505".equals(sql.getSQLState())
                && sql.getMessage().contains(name);
    }
}
