package com.myks.myksbk.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class UserMeDto {
    @Getter
    @Setter
    public static class Response {
        public Long id;
        public Long companyId;
        public String account;
        public String publicId;
        public String name;
        public String profile_name;
        public String email;
        public String extension;
        public String status;
        public String createdAt;
        public String deactivatedAt;
        public String updatedAt;
    }

    @Getter @Setter
    public static class UpdateRequest {
        private String account;
        private String name;

        // 프론트에서 "profile_name"으로 보내므로 매핑 필요
        @JsonProperty("profile_name")
        private String profileName;

        private String email;
        private String status;
    }
}