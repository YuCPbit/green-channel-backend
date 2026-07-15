package edu.greenchannel.student;

public record StudentImportError(int rowNumber, String studentNo, String reason) {
}
