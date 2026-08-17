package com.adarsh.ridebookingsocketservice.controller;

import com.adarsh.ridebookingsocketservice.dtos.RideRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/socket")
public class DriverRequestController {

 private final SimpMessagingTemplate simpMessagingTemplate;

 public DriverRequestController(SimpMessagingTemplate simpMessagingTemplate) {
    this.simpMessagingTemplate = simpMessagingTemplate;
 }

 @PostMapping("/newride")
 public ResponseEntity<Boolean> createRideRequest(@RequestBody RideRequestDto rideRequestDto) {
      sendToDriversNewRideRequest(rideRequestDto);
      return new  ResponseEntity<>(true, HttpStatus.OK);
 }

 public void sendToDriversNewRideRequest(RideRequestDto rideRequestDto) {
    System.out.println("Sending ride request to drivers");
    simpMessagingTemplate.convertAndSend("/topic/rideRequest",rideRequestDto);
 }


}
