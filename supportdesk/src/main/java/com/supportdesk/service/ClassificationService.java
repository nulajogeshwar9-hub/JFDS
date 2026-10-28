package com.supportdesk.service;

import com.supportdesk.dto.ClassificationSuggestionDTO;

public interface ClassificationService {
    ClassificationSuggestionDTO classify(Long orgId, String subject, String description, String domain);
}

