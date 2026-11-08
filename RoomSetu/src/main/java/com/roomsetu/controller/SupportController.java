package com.roomsetu.controller;

import com.roomsetu.dto.SupportQueryRequest;
import com.roomsetu.dto.SupportQueryResponse;
import com.roomsetu.service.FaqAutoResponseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final FaqAutoResponseService faqAutoResponseService;

    public SupportController(FaqAutoResponseService faqAutoResponseService) {
        this.faqAutoResponseService = faqAutoResponseService;
    }

    @PostMapping("/ask")
    public ResponseEntity<SupportQueryResponse> ask(@Valid @RequestBody SupportQueryRequest request) {
        return ResponseEntity.ok(faqAutoResponseService.answer(request.getQuestion()));
    }
}