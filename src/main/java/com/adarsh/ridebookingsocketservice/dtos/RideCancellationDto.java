package com.adarsh.ridebookingsocketservice.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RideCancellationDto {
    private Long bookingId;
    private String message;
}
