package com.adarsh.ridebookingsocketservice.dtos;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalBookingUpdateRequestDto {
    private Long driverId;
}
