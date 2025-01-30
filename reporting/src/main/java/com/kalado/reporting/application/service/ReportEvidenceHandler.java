package com.kalado.reporting.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportEvidenceHandler {
    private final EvidenceService evidenceService;

    public List<String> processEvidenceFiles(List<MultipartFile> evidenceFiles) {
        if (evidenceFiles == null || evidenceFiles.isEmpty()) {
            return Collections.emptyList();
        }

        return evidenceService.storeEvidence(evidenceFiles);
    }
}
