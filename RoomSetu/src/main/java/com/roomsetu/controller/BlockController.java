package com.roomsetu.controller;

import com.roomsetu.dto.BlockedUserResponse;
import com.roomsetu.service.BlockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    @PostMapping("/{blockedUserId}")
    public ResponseEntity<Void> block(@AuthenticationPrincipal Long blockerId,
                                      @PathVariable Long blockedUserId) {
        blockService.block(blockerId, blockedUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{blockedUserId}")
    public ResponseEntity<Void> unblock(@AuthenticationPrincipal Long blockerId,
                                        @PathVariable Long blockedUserId) {
        blockService.unblock(blockerId, blockedUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<BlockedUserResponse>> mine(@AuthenticationPrincipal Long blockerId) {
        return ResponseEntity.ok(blockService.myBlocks(blockerId));
    }
}