package com.adarsh.ridebookingsocketservice.dtos;

import lombok.*;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateBookingRequestDto {
    private String status;
    private Optional<Long> driverId;
}
