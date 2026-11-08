package com.roomsetu.service;

import com.roomsetu.dto.ProfileRequest;
import com.roomsetu.entity.Profile;
import com.roomsetu.entity.User;
import com.roomsetu.repository.ProfileRepository;
import com.roomsetu.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;



    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    public Profile upsert(Long userId, ProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Profile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> Profile.builder().user(user).build());

        profile.setBudgetMin(req.getBudgetMin());
        profile.setBudgetMax(req.getBudgetMax());
        profile.setLocality(req.getLocality());
        profile.setSleepSchedule(req.getSleepSchedule());
        profile.setWorkFromHome(req.getWorkFromHome());
        profile.setFoodPreference(req.getFoodPreference());
        profile.setCleanliness(req.getCleanliness());
        profile.setSmokes(req.getSmokes());
        profile.setDrinks(req.getDrinks());
        profile.setHasPets(req.getHasPets());
        profile.setOkWithPets(req.getOkWithPets());
        profile.setBio(req.getBio());
        profile.setPhotoUrl(req.getPhotoUrl());

        boolean hasLat = req.getLatitude() != null;
        boolean hasLng = req.getLongitude() != null;
        if (hasLat != hasLng) {
            throw new IllegalArgumentException("Location needs both latitude and longitude");
        }
        if (hasLat && (req.getLatitude() < -90 || req.getLatitude() > 90
                || req.getLongitude() < -180 || req.getLongitude() > 180)) {
            throw new IllegalArgumentException("That location is out of range");
        }
        profile.setLatitude(req.getLatitude());
        profile.setLongitude(req.getLongitude());
// Sending no coordinates clears the saved location, and the label with it.
        profile.setLocationLabel(hasLat ? req.getLocationLabel() : null);

        return profileRepository.save(profile);
    }

    public Profile getByUserId(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
    }
}