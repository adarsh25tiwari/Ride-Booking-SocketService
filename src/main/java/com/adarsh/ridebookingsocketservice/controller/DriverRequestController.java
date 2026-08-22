package com.adarsh.ridebookingsocketservice.controller;

import com.adarsh.ridebookingsocketservice.dtos.InternalBookingUpdateRequestDto;
import com.adarsh.ridebookingsocketservice.dtos.RideRequestDto;
import com.adarsh.ridebookingsocketservice.dtos.RideResponseDto;
import org.springframework.http.*;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;



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

 @PostMapping("/newRide")
 @CrossOrigin(originPatterns = "*")
 public ResponseEntity<Boolean> createRideRequest(@RequestBody RideRequestDto rideRequestDto) {
      sendToDriversNewRideRequest(rideRequestDto);
      return new  ResponseEntity<>(true, HttpStatus.OK);
 }


 public void sendToDriversNewRideRequest(RideRequestDto rideRequestDto) {
    System.out.println("Sending ride request to drivers");
    simpMessagingTemplate.convertAndSend("/topic/rideRequest",rideRequestDto);
 }

    @MessageMapping("/rideResponse/{userId}")
    public synchronized void rideResponseHandler(@DestinationVariable String userId, RideResponseDto rideResponseDto) {

        System.out.println("Driver " + userId +" response: " + rideResponseDto.getResponse());

        // Driver rejected the ride
        if (!Boolean.TRUE.equals(rideResponseDto.getResponse())) {
            System.out.println("Driver rejected the ride");
            return;
        }

        // Driver accepted the ride
        InternalBookingUpdateRequestDto requestDto = InternalBookingUpdateRequestDto.builder()
                        .driverId(Long.parseLong(userId))
                        .bookingStatus("SCHEDULED")
                        .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<InternalBookingUpdateRequestDto> requestEntity = new HttpEntity<>(requestDto, headers);
        ResponseEntity<String> result = restTemplate.exchange(
                        "http://localhost:9096/api/v1/booking/internal/"
                                + rideResponseDto.getBookingId(),
                        HttpMethod.PATCH,
                        requestEntity,
                        String.class
                );

        System.out.println("Booking Service response: " + result.getStatusCode());
    }
}
