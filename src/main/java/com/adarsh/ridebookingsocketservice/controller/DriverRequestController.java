package com.adarsh.ridebookingsocketservice.controller;

import com.adarsh.ridebookingsocketservice.dtos.InternalBookingUpdateRequestDto;
import com.adarsh.ridebookingsocketservice.dtos.RideCancellationDto;
import com.adarsh.ridebookingsocketservice.dtos.RideRequestDto;
import com.adarsh.ridebookingsocketservice.dtos.RideResponseDto;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@RestController
@RequestMapping("/api/v1/socket")
public class DriverRequestController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RestTemplate restTemplate;

    public DriverRequestController(SimpMessagingTemplate simpMessagingTemplate,
                                   RestTemplate restTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.restTemplate = restTemplate;
    }

    private final Map<Long, List<Long>> activeRideRequests = new ConcurrentHashMap<>();

    @PostMapping("/newRide")
    @CrossOrigin(originPatterns = "*")
    public ResponseEntity<Boolean> createRideRequest(@RequestBody RideRequestDto rideRequestDto) {
        sendToDriversNewRideRequest(rideRequestDto);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }


    public void sendToDriversNewRideRequest(RideRequestDto rideRequestDto) {
        System.out.println("Sending ride request to nearby drivers");

        // Store which drivers received this booking
        activeRideRequests.put(rideRequestDto.getBookingId(), rideRequestDto.getDriverIds());


        for (Long driverId : rideRequestDto.getDriverIds()) {
            String topic = "/topic/rideRequest/" + driverId;
            System.out.println("Sending booking " + rideRequestDto.getBookingId() + " to driver " + driverId);
            System.out.println("ACTUAL TOPIC = " + topic);
            simpMessagingTemplate.convertAndSend(topic, rideRequestDto);
        }

    }

    @MessageMapping("/rideResponse/{userId}")
    public synchronized void rideResponseHandler(@DestinationVariable String userId, RideResponseDto rideResponseDto) {
        System.out.println("Driver " + userId + " response: " + rideResponseDto.getResponse());

        // Driver rejected the ride
        if (!Boolean.TRUE.equals(rideResponseDto.getResponse())) {
            System.out.println("Driver rejected the ride");
            return;
        }
        Long driverId = Long.parseLong(userId);
        Long bookingId = rideResponseDto.getBookingId();

        // Driver accepted the ride
        InternalBookingUpdateRequestDto requestDto = InternalBookingUpdateRequestDto.builder()
                .driverId(driverId)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<InternalBookingUpdateRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);

        ResponseEntity<String> result = restTemplate.exchange(
                "http://localhost:9096/api/v1/booking/internal/"
                        + bookingId,
                HttpMethod.PATCH,
                requestEntity,
                String.class);

        System.out.println("Booking Service response: " + result.getStatusCode());

        // Only cancel other drivers if this driver successfully got the booking.
        if (result.getStatusCode().is2xxSuccessful()) {
            cancelRideForOtherDrivers(bookingId, driverId);
        } else {
            System.out.println("Driver " + driverId + " did not get booking " + bookingId);
        }
    }


    private void cancelRideForOtherDrivers(Long bookingId, Long acceptedDriverId) {
        List<Long> driverIds = activeRideRequests.remove(bookingId);
        if (driverIds == null) {
            System.out.println("No active ride request found for booking " + bookingId);
            return;
        }
        System.out.println("Cancelling booking " + bookingId + " for remaining drivers");

        for (Long driverId : driverIds) {
            // Don't cancel the driver who accepted
            if (driverId.equals(acceptedDriverId)) {
                continue;
            }

            String topic = "/topic/rideRequest/" + driverId;
            RideCancellationDto cancellationDto = new RideCancellationDto(
                            bookingId,
                            "Ride is no longer available");
            System.out.println("Cancelling booking " + bookingId + " for driver " + driverId);
            simpMessagingTemplate.convertAndSend(topic, cancellationDto);
        }
    }
}
