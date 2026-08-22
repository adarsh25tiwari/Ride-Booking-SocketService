package com.adarsh.ridebookingsocketservice.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RideRequestDto {
    //private Long passengerId;
    private List<Long> driverIds;   // ride request will send to drivers anyone can accept ride
    private Long bookingId;
}
