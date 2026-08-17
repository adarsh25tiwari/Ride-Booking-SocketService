package com.adarsh.ridebookingsocketservice.dtos;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RideResponseDto {
    private Boolean response;
    private Long driverId;
}
