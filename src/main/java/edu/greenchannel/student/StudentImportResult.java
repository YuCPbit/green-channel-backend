package edu.greenchannel.student;

import java.util.List;

public record StudentImportResult(
        int totalRows,
        int importedRows,
        int failedRows,
        boolean activationRequired,
        String errorReportId,
        List<StudentImportError> errors) {
}
