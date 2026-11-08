package com.roomsetu.service;

import com.roomsetu.dto.ListingResponse;
import com.roomsetu.dto.PublicProfileResponse;
import com.roomsetu.dto.ReviewResponse;
import com.roomsetu.entity.Match;
import com.roomsetu.entity.Profile;
import com.roomsetu.entity.User;
import com.roomsetu.entity.enums.MatchStatus;
import com.roomsetu.repository.MatchRepository;
import com.roomsetu.repository.ProfileRepository;
import com.roomsetu.repository.ReviewRepository;
import com.roomsetu.repository.UserRepository;
import com.roomsetu.util.EmailDomains;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PublicProfileService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ReviewRepository reviewRepository;
    private final MatchRepository matchRepository;
    private final ListingService listingService;
    private final BlockService blockService;

    public PublicProfileService(UserRepository userRepository, ProfileRepository profileRepository,
                                ReviewRepository reviewRepository, MatchRepository matchRepository,
                                ListingService listingService, BlockService blockService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.reviewRepository = reviewRepository;
        this.matchRepository = matchRepository;
        this.listingService = listingService;
        this.blockService = blockService;
    }

    private static String name(Enum<?> e) {
        return e == null ? null : e.name();
    }

    public PublicProfileResponse get(Long viewerId, Long targetId) {
        User user = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        boolean self = viewerId.equals(targetId);

        if (!self && blockService.isBlockedEitherDirection(viewerId, targetId)) {
            throw new IllegalArgumentException("This profile isn't available");
        }

        Profile profile = profileRepository.findByUserId(targetId).orElseGet(Profile::new);

        List<ReviewResponse> reviews = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(targetId)
                .stream().limit(10).map(ReviewResponse::from).toList();

        List<ListingResponse> listings = listingService.browseVerifiedByHost(targetId)
                .stream().map(ListingResponse::from).toList();

        Optional<Match> match = self ? Optional.empty() : matchRepository.findBetween(viewerId, targetId);
        String matchStatus = match.map(m -> m.getStatus().name()).orElse(null);
        boolean canReview = match.filter(m -> m.getStatus() == MatchStatus.ACCEPTED)
                .map(m -> !reviewRepository.existsByReviewerIdAndRevieweeIdAndMatchId(viewerId, targetId, m.getId()))
                .orElse(false);

        return PublicProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .photoUrl(profile.getPhotoUrl())
                .bio(profile.getBio())
                .locality(profile.getLocality())
                .memberSince(user.getCreatedAt())
                .self(self)
                .sleepSchedule(name(profile.getSleepSchedule()))
                .workFromHome(name(profile.getWorkFromHome()))
                .foodPreference(name(profile.getFoodPreference()))
                .cleanliness(name(profile.getCleanliness()))
                .smokes(profile.getSmokes())
                .drinks(profile.getDrinks())
                .hasPets(profile.getHasPets())
                .phoneVerified(true) // every account signs in with a phone OTP
                .workEmailDomain(EmailDomains.verifiedDomain(user))
                .averageRating(reviewRepository.averageRatingFor(targetId))
                .reviewCount(reviewRepository.countByRevieweeId(targetId))
                .reviews(reviews)
                .listings(listings)
                .matchStatus(matchStatus)
                .viewerCanReview(canReview)
                .build();
    }
}