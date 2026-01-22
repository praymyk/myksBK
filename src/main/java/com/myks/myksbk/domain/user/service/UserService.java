package com.myks.myksbk.domain.user.service;

import com.myks.myksbk.domain.user.domain.User;
import com.myks.myksbk.domain.user.dto.UserMeDto;
import com.myks.myksbk.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserMeDto.Response getMe(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user not found"));

        UserMeDto.Response dto = new UserMeDto.Response();
        dto.id = u.getId();
        dto.account = u.getAccount();
        dto.public_id = u.getPublicId();
        dto.name = u.getName();
        dto.profile_name = u.getProfileName();
        dto.email = u.getEmail();
        dto.extension = u.getExtension();
        dto.status = u.getStatus().name();
        dto.created_at = u.getCreatedAt() != null ? u.getCreatedAt().toString() : null;
        dto.deactivated_at = u.getDeactivatedAt() != null ? u.getDeactivatedAt().toString() : null;
        dto.updated_at = u.getUpdatedAt() != null ? u.getUpdatedAt().toString() : null;
        return dto;
    }
}
