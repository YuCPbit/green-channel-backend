package edu.workstudy.controller;

import edu.workstudy.common.Result;
import edu.workstudy.service.WorkStudyAgreementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agreement")
@RequiredArgsConstructor
public class WorkStudyAgreementController {

    private final WorkStudyAgreementService agreementService;

    @PostMapping("/sign")
    public Result<String> sign(@RequestParam Long agreementId,
                               @RequestParam Long studentId) {
        agreementService.signAgreement(agreementId, studentId);
        return Result.success("签署成功");
    }
}