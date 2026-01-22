package com.myks.myksbk.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

public class UserMeDto {
    @Getter
    @Setter
    public static class Response {
        public Long id;
        public String account;
        public String public_id;
        public String name;
        public String profile_name;
        public String email;
        public String extension;
        public String status;
        public String created_at;
        public String deactivated_at;
        public String updated_at;
    }
}