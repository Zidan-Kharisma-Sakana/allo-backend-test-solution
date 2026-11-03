package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.CreateExpenseRequest;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.MessageDigest;
import java.util.HexFormat;

final class ExpenseFingerprint {
    private ExpenseFingerprint() {}

    static String of(CreateExpenseRequest request) {
        try {
            var bytes = new ByteArrayOutputStream();
            var data = new DataOutputStream(bytes);
            data.writeUTF(request.payerId().toString());
            data.writeUTF(request.amount().stripTrailingZeros().toPlainString());
            data.writeBoolean(request.description() != null);
            if (request.description() != null) data.writeUTF(request.description());
            var participants = request.participantIds().stream().map(Object::toString).sorted().toList();
            data.writeInt(participants.size());
            for (String participant : participants) data.writeUTF(participant);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray()));
        } catch (java.io.IOException | java.security.NoSuchAlgorithmException failure) {
            throw new IllegalStateException("Could not fingerprint expense", failure);
        }
    }
}
