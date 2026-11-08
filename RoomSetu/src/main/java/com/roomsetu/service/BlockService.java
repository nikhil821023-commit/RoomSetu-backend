package com.roomsetu.service;

import com.roomsetu.dto.BlockedUserResponse;
import com.roomsetu.entity.Block;
import com.roomsetu.entity.User;
import com.roomsetu.repository.BlockRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockService {

    private final BlockRepository blockRepository;
    private final UserRepository userRepository;

    public BlockService(BlockRepository blockRepository, UserRepository userRepository) {
        this.blockRepository = blockRepository;
        this.userRepository = userRepository;
    }

    public void block(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("You can't block yourself");
        }
        if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            return;
        }
        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new IllegalArgumentException("User to block not found"));
        blockRepository.save(Block.builder().blocker(blocker).blocked(blocked).build());
    }

    public void unblock(Long blockerId, Long blockedId) {
        blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
                .ifPresent(blockRepository::delete);
    }

    public boolean isBlockedEitherDirection(Long userA, Long userB) {
        return blockRepository.existsByBlockerIdAndBlockedId(userA, userB)
                || blockRepository.existsByBlockerIdAndBlockedId(userB, userA);
    }

    /** Same message in both directions, so a blocked person isn't told who blocked whom. */
    public void requireNotBlocked(Long userA, Long userB) {
        if (isBlockedEitherDirection(userA, userB)) {
            throw new IllegalArgumentException("You can't interact with this user");
        }
    }

    public List<BlockedUserResponse> myBlocks(Long blockerId) {
        return blockRepository.findByBlockerIdOrderByCreatedAtDesc(blockerId).stream()
                .map(b -> new BlockedUserResponse(b.getBlocked().getId(), b.getBlocked().getName()))
                .toList();
    }
}