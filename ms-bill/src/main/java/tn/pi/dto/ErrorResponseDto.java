package tn.pi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;


public class ErrorResponseDto {
    private String apiPath;
    private HttpStatus status;
    private String errorMessage;
    private LocalDateTime errorTime;

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(LocalDateTime errorTime) {
        this.errorTime = errorTime;
    }

    public ErrorResponseDto(String apiPath, HttpStatus status, String errorMessage, LocalDateTime errorTime) {
        this.apiPath = apiPath;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorTime = errorTime;
    }
}
