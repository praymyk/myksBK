package com.myks.myksbk.domain.user.service;

import com.myks.myksbk.domain.user.domain.User;
import com.myks.myksbk.domain.user.domain.UserStatus;
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
        dto.companyId = u.getCompanyId();
        dto.account = u.getAccount();
        dto.publicId = u.getPublicId();
        dto.name = u.getName();
        dto.profile_name = u.getProfileName();
        dto.email = u.getEmail();
        dto.extension = u.getExtension();
        dto.status = u.getStatus().name();
        dto.createdAt = u.getCreatedAt() != null ? u.getCreatedAt().toString() : null;
        dto.deactivatedAt = u.getDeactivatedAt() != null ? u.getDeactivatedAt().toString() : null;
        dto.updatedAt = u.getUpdatedAt() != null ? u.getUpdatedAt().toString() : null;
        return dto;
    }

    @Transactional
    public void updateProfile(Long userId, UserMeDto.UpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));


        if (request.getAccount() != null) user.setAccount(request.getAccount());
        if (request.getName() != null) user.setName(request.getName());
        if (request.getProfileName() != null) user.setProfileName(request.getProfileName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getStatus() != null) {
            try {
                UserStatus newStatus = UserStatus.valueOf(request.getStatus());
                user.setStatus(newStatus);
            } catch (IllegalArgumentException e) {

                throw new IllegalArgumentException("잘못된 회원 상태값입니다: " + request.getStatus());
            }
        }
    }
}
