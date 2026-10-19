package com.rtech.mandiplus.dto;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String role;
    private String profilePhoto;
    private String name;
    private String phone;
    private Boolean isProfileComplete = true;
    private Boolean profileSetupRequired = false;

    public JwtResponse() {
    }

    public JwtResponse(String accessToken, Long id, String email, String role, String profilePhoto) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.role = role;
        this.profilePhoto = profilePhoto;
    }

    public JwtResponse(String token, String type, Long id, String email, String role, String profilePhoto) {
        this.token = token;
        this.type = type;
        this.id = id;
        this.email = email;
        this.role = role;
        this.profilePhoto = profilePhoto;
    }

    public JwtResponse(String token, Long id, String email, String role, String profilePhoto, String name, String phone, Boolean isProfileComplete) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.role = role;
        this.profilePhoto = profilePhoto;
        this.name = name;
        this.phone = phone;
        this.isProfileComplete = isProfileComplete != null ? isProfileComplete : false;
        this.profileSetupRequired = !this.isProfileComplete;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getIsProfileComplete() {
        return isProfileComplete;
    }

    public void setIsProfileComplete(Boolean isProfileComplete) {
        this.isProfileComplete = isProfileComplete;
        this.profileSetupRequired = !Boolean.TRUE.equals(isProfileComplete);
    }

    public Boolean getProfileSetupRequired() {
        return profileSetupRequired;
    }

    public void setProfileSetupRequired(Boolean profileSetupRequired) {
        this.profileSetupRequired = profileSetupRequired;
    }
}
