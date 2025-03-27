package com.kafka_impl.service.producer_service.service;

import com.kafka_impl.service.producer_service.model.Location;
import com.kafka_impl.service.producer_service.model.LocationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleRouteService {

    @Value("${google.maps.api.key}")
    private String googleApiKey;

    private Logger logger = LoggerFactory.getLogger(GoogleRouteService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    public List<double[]> getDirections(LocationDTO location){
        double startLat = location.getStartLatitude();
        double startLng = location.getStartLongitude();
        double endLat = location.getEndLatitude();
        double endLng = location.getEndLongitude();
        try {

            String url = String.format(
                    "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&key=%s",
                    startLat, startLng, endLat, endLng, googleApiKey
            );
            String response = restTemplate.getForObject(url, String.class);
//            logger.info("Response from GOOGLE ROUTE API : " + response);
            return extractCoordinates(response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<double[]>();
        }
    }

    private List<double[]> extractCoordinates(String response) {
        List<double[]> coordinates = new ArrayList<>();
        JSONObject jsonResponse = new JSONObject(response);

        if (!jsonResponse.has("routes")) {
            return coordinates;
        }

        JSONArray routes = jsonResponse.getJSONArray("routes");
        for (int i = 0; i < routes.length(); i++) {
            JSONArray legs = routes.getJSONObject(i).getJSONArray("legs");
            for (int j = 0; j < legs.length(); j++) {
                JSONArray steps = legs.getJSONObject(j).getJSONArray("steps");
                for (int k = 0; k < steps.length(); k++) {
                    JSONObject startLocation = steps.getJSONObject(k).getJSONObject("start_location");
                    JSONObject endLocation = steps.getJSONObject(k).getJSONObject("end_location");
                    coordinates.add(new double[]{startLocation.getDouble("lng"), startLocation.getDouble("lat")});
                    coordinates.add(new double[]{endLocation.getDouble("lng"), endLocation.getDouble("lat")});
                }
            }
        }
        return coordinates;
    }
}
