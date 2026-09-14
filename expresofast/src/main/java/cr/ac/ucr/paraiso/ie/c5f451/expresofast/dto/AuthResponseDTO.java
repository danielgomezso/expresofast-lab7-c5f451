package cr.ac.ucr.paraiso.ie.c5f451.expresofast.dto;

import java.util.List;

public class AuthResponseDTO {

    private String token;
    private String username;
    private List<String> roles;
    private Long expirationTime;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, String username, List<String> roles, Long expirationTime) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.expirationTime = expirationTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }
}