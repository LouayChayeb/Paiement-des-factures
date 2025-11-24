package tn.pi.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ResponseDTO {
    private String StatusCode ;
    private String StatusMessage ;
}