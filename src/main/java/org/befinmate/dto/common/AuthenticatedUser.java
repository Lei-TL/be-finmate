package org.befinmate.dto.common;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticatedUser {

    private String userId;
    private String email;

}
