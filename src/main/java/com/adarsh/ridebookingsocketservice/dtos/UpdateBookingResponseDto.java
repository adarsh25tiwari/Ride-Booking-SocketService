package com.adarsh.ridebookingsocketservice.dtos;

import com.adarsh.RideBooking_EntityService.models.BookingStatus;
import com.adarsh.RideBooking_EntityService.models.Driver;
import lombok.*;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateBookingResponseDto {
    private Long bookingId;
    private BookingStatus bookingStatus;
    private Optional<Driver> driver;
}
