package edu.greenchannel.student;

import java.util.List;

public record StudentBatchDeleteRequest(List<Long> ids) {
}
