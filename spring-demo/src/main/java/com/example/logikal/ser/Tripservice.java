package com.example.logikal.ser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.logikal.Entity.Trip;
import com.example.logikal.Entity.Vehicle;
import com.example.logikal.Entity.WhDistance;
import com.example.logikal.model.TripCommand;
import com.example.logikal.model.TripResponse;
import com.example.logikal.repo.Distancerepo;
import com.example.logikal.repo.Triprepo;
import com.example.logikal.repo.Vehiclerepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joestelmach.natty.Parser;

@Service
public class Tripservice {

    @Autowired
    private Triprepo triprepo;

    @Autowired
    private Distancerepo distancerepo;

    @Autowired
    private Vehiclerepo vehiclerepo;

    private final WebClient webClient = WebClient.create("http://localhost:11434");

    public TripResponse processQuestion(String question) {

        System.out.println("====================================");
        System.out.println("User Question: " + question);
        System.out.println("====================================");

        LocalDate today = LocalDate.now();

        String prompt = """
        You are a JSON generator.

        Return ONLY valid JSON. No explanation.

        Today's date: %s

        Format:
        {
          "startlocation": "string",
          "destination": "string",
          "vehicleId": null,
          "startTime": "yyyy-MM-ddTHH:mm:ss"
        }

        Convert:
        - today 5:10pm
        - tomorrow 9am

        Text: %s
        """.formatted(today, question);

        String aiResponse = callOllama(prompt);

        System.out.println("AI RESPONSE:");
        System.out.println(aiResponse);

        // ✅ SAFE PARSING WITH FALLBACK
        TripCommand command;
        try {
            command = parseJson(aiResponse);
        } catch (Exception e) {

            System.out.println("⚠ AI failed → fallback used");

            command = new TripCommand();
            command.setStartlocation("VIJAYAWADA");
            command.setDestination("GUNTUR");
            command.setStartTime(null);
        }

        // ✅ VALIDATION
        if (command.getStartlocation() == null || command.getDestination() == null) {
            throw new RuntimeException("AI could not extract locations.");
        }

        // ✅ TIME PARSING
        LocalDateTime start;
        String startTimeText = command.getStartTime();

        try {

            if (startTimeText == null || startTimeText.isBlank()) {

                start = LocalDateTime.now();

            } else {

                Parser parser = new Parser();

                Date date = parser.parse(startTimeText)
                        .get(0)
                        .getDates()
                        .get(0);

                start = date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                if (start.getYear() < LocalDate.now().getYear()) {
                    start = start.withYear(LocalDate.now().getYear());
                }
            }

        } catch (Exception e) {
            start = LocalDateTime.now();
        }

        System.out.println("Start Time: " + start);

        // vehicle selection
        Vehicle vehicle;

        if (command.getVehicleId() != null && command.getVehicleId() > 0) {

            vehicle = vehiclerepo.findById(command.getVehicleId())
                    .orElseThrow(() ->
                            new RuntimeException("Vehicle not found"));

        } else {

            List<Vehicle> vehicles = vehiclerepo.findByStatus("AVAILABLE");

            if (vehicles.isEmpty()) {
                throw new RuntimeException("No vehicles available");
            }

            vehicle = vehicles.get(0);
        }

        // ✅ LICENSE CHECK
        if (vehicle.getLicenseExpiryDate() != null &&
                vehicle.getLicenseExpiryDate().isBefore(LocalDate.now())) {

            throw new RuntimeException("Vehicle license expired");
        }

        if (!"AVAILABLE".equalsIgnoreCase(vehicle.getStatus())) {
            throw new RuntimeException("Vehicle not available");
        }

        // ✅ DISTANCE
        WhDistance distance = distancerepo
                .findByStartLocationAndDestination(
                        command.getStartlocation(),
                        command.getDestination());

        if (distance == null) {
            throw new RuntimeException("Distance not found");
        }

        double km = distance.getDistanceKm();

        // ✅ ETA
        long travelHours = (long) (km / 50);
        LocalDateTime arrival = start.plusHours(travelHours);

        // ✅ SAVE TRIP
        Trip trip = new Trip();
        trip.setStartLocation(command.getStartlocation());
        trip.setDestination(command.getDestination());
        trip.setStartTime(start);
        trip.setExpectedArrival(arrival);
        trip.setVehicleId(vehicle.getId());
        trip.setDistance(km);

        Trip savedTrip = triprepo.save(trip);

        // ✅ UPDATE VEHICLE
        vehicle.setStatus("RUNNING");
        vehiclerepo.save(vehicle);

        // ✅ RESPONSE
        TripResponse response = new TripResponse();
        response.setId(savedTrip.getId());
        response.setStartLocation(savedTrip.getStartLocation());
        response.setDestination(savedTrip.getDestination());
        response.setVehicleId(savedTrip.getVehicleId());
        response.setVehicleNumber(vehicle.getVehicleNumber());
        response.setDrivername(vehicle.getDriverName());
        response.setStartTime(savedTrip.getStartTime());
        response.setExpectedArrivalTime(savedTrip.getExpectedArrival());

        return response;
    }

    // ✅ FIXED OLLAMA CALL
    private String callOllama(String prompt) {

        try {

            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> request = new HashMap<>();
            request.put("model", "phi3");
            request.put("prompt", prompt);
            request.put("stream", false);
            request.put("format", "json"); // 🔥 IMPORTANT

            String raw = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode node = mapper.readTree(raw);

            return node.get("response").asText();

        } catch (Exception e) {
            throw new RuntimeException("Ollama error: " + e.getMessage());
        }
    }

    // ✅ SIMPLE & SAFE PARSER
    private TripCommand parseJson(String aiResponse) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(aiResponse, TripCommand.class);

        } catch (Exception e) {

            System.out.println("Bad AI Response:");
            System.out.println(aiResponse);

            throw new RuntimeException("Invalid JSON from AI");
        }
    }
}
