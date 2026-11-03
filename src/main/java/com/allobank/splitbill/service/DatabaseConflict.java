package com.allobank.splitbill.service;

final class DatabaseConflict {
    private DatabaseConflict() {}

    static boolean isConstraint(Throwable failure, String name) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation
                    && name.equals(violation.getConstraintName())) return true;
            if (cause instanceof java.sql.SQLException sql
                    && "23505".equals(sql.getSQLState()) && sql.getMessage().contains(name)) return true;
        }
        return false;
    }
}
