# 🔌 Ride Booking — Socket Service

Real-time communication service responsible for delivering ride requests to nearby drivers and receiving driver responses using **WebSocket and STOMP**.

The Socket Service separates real-time communication from the core booking business logic.

---

## 🎯 Responsibilities

* Establish WebSocket connections
* Handle STOMP messages
* Send ride requests to individual drivers
* Receive driver ride responses
* Forward driver acceptance to Booking Service
* Provide driver-specific WebSocket destinations

---

## 🛠️ Tech Stack

| Technology       | Purpose                           |
| ---------------- | --------------------------------- |
| Java             | Programming language              |
| Spring Boot      | Backend framework                 |
| Spring WebSocket | WebSocket support                 |
| STOMP            | Messaging protocol                |
| SockJS           | WebSocket fallback/client support |
| RestTemplate     | Internal service communication    |
| Gradle           | Build tool                        |

---

# 🏗️ Architecture

```text
                    Booking Service
                          │
                          │ Ride Request
                          ▼
                  ┌─────────────────┐
                  │ Socket Service  │
                  │                 │
                  │ STOMP Broker    │
                  └────────┬────────┘
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
         Driver 1      Driver 2      Driver 3
             │             │             │
             │             │             │
             └─────────────┼─────────────┘
                           │
                     Driver Response
                           │
                           ▼
                    Socket Service
                           │
                           │ Internal API
                           ▼
                    Booking Service
```

---

# 📡 Ride Request Flow

When Booking Service discovers nearby drivers, it sends a ride request to Socket Service.

```text
Booking Service
      │
      │ RideRequestDto
      ▼
Socket Service
      │
      ├──► /topic/rideRequest/1
      ├──► /topic/rideRequest/2
      └──► /topic/rideRequest/3
```

Each driver receives the request on their own destination.

---

# 🎯 Driver-Specific Topics

The Socket Service uses destinations such as:

```text
/topic/rideRequest/{driverId}
```

Examples:

```text
/topic/rideRequest/1
/topic/rideRequest/2
/topic/rideRequest/3
```

This allows ride requests to be targeted to specific drivers.

---

# 🚗 Driver Acceptance Flow

When a driver accepts a ride:

```text
Driver
  │
  │ WebSocket message
  ▼
Socket Service
  │
  │ Driver ID + Booking ID
  ▼
Booking Service
  │
  ▼
Atomic Driver Assignment
```

The Socket Service does not decide which driver ultimately owns the booking.

That decision belongs to Booking Service and its database.

---

# 🔒 Separation of Responsibilities

This separation is intentional.

```text
Socket Service
    │
    └── Real-time communication

Booking Service
    │
    └── Booking business logic
        + driver assignment
        + booking state
```

Therefore:

```text
Socket Service ≠ Booking ownership
```

The Socket Service forwards the driver's response, while Booking Service performs the authoritative assignment.

---

# 🧵 Concurrent Driver Acceptance

Multiple drivers may accept the same ride almost simultaneously.

```text
Driver 1 ─────┐
Driver 2 ─────┼──► Socket Service
Driver 3 ─────┘
                    │
                    ▼
              Booking Service
                    │
                    ▼
             Atomic Assignment
```

Socket Service forwards these responses independently.

Booking Service determines the winner using its atomic database operation.

---

# 🌐 WebSocket Endpoint

The WebSocket connection is established through the configured WebSocket endpoint.

The application uses:

```text
/websocket
```

STOMP destinations are then used for ride communication.

---

# 🧪 Testing

The Socket Service was tested using a WebSocket client/HTML test page.

Multiple browser tabs can represent different drivers.

Example:

```text
Browser Tab 1 → Driver 1
Browser Tab 2 → Driver 2
Browser Tab 3 → Driver 3
```

Each driver subscribes to:

```text
/topic/rideRequest/{driverId}
```

This makes it possible to demonstrate real-time ride distribution and concurrent acceptance.

---

# 🧠 Key Design Decisions

* Dedicated service for real-time communication
* STOMP over WebSocket
* Driver-specific destinations
* Booking logic remains inside Booking Service
* Driver acceptance is forwarded to Booking Service
* Supports concurrent driver responses

---

# 🔮 Future Improvements

* Remove/cancel ride requests for losing drivers
* Disconnect inactive drivers
* Driver connection management
* Redis-backed WebSocket session tracking
* WebSocket authentication
* Message acknowledgements
* Kafka/event-driven integration
* Horizontal WebSocket scaling

---

## 🔗 Related Services

* [Booking Service](https://github.com/adarsh25tiwari/Ride-Booking-BookingService)
* [Location Service](https://github.com/adarsh25tiwari/Ride-Booking-LocationService)
* [Auth Service](https://github.com/adarsh25tiwari/Ride-Booking-AuthService)
* [Review Service](https://github.com/adarsh25tiwari/Ride-Booking-ReviewService)
* [Service Discovery](https://github.com/adarsh25tiwari/Ride-Booking-ServiceDiscovery)
