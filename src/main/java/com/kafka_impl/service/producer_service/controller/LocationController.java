package com.kafka_impl.service.producer_service.controller;

import com.kafka_impl.service.producer_service.config.AppConstants;
import com.kafka_impl.service.producer_service.model.Location;
import com.kafka_impl.service.producer_service.model.LocationDTO;
import com.kafka_impl.service.producer_service.service.GoogleRouteService;
import com.kafka_impl.service.producer_service.service.KafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Autowired
    private KafkaService kafkaService;

    private Logger logger = LoggerFactory.getLogger(LocationController.class);

    @Autowired
    private GoogleRouteService googleRouteService;

    @CrossOrigin(origins = "*")
    @PostMapping("/update")
    public ResponseEntity<?> updateLocation(@RequestBody LocationDTO location) throws InterruptedException {
        List<double[]> route = googleRouteService.getDirections(location);
        List<Location> routeLocations = new ArrayList<>();
        for(double[] coordinates : route){
            logger.info(coordinates[0] + "," + coordinates[1]);
            double longitude = coordinates[0];
            double latitude = coordinates[1];
            Location routeLocation = new Location(location.getId(),longitude,latitude);
            routeLocations.add(routeLocation);
        }
        //For simulation purpose we would be sending only 10 coordinates along the route;
        if(routeLocations.size() < 10){
            for(Location routeLocation : routeLocations){

                if(this.kafkaService.updateLocation(routeLocation)){
                    routeLocation.setMessage(AppConstants.SUCCESS_MSG);
                }else{
                    routeLocation.setMessage(AppConstants.FAIL_MSG);
                }
                Thread.sleep(5000);
            }
        }else{
            // Process the first 5 elements
            for (int i = 0; i < 5; i++) {
                if (this.kafkaService.updateLocation(routeLocations.get(i))) {
                    routeLocations.get(i).setMessage(AppConstants.SUCCESS_MSG);
                } else {
                    routeLocations.get(i).setMessage(AppConstants.FAIL_MSG);
                }
                Thread.sleep(5000);
            }

            // Process the last 5 elements
            int size = routeLocations.size();
            for (int i = size - 5; i < size; i++) {
                if (this.kafkaService.updateLocation(routeLocations.get(i))) {
                    routeLocations.get(i).setMessage(AppConstants.SUCCESS_MSG);
                } else {
                    routeLocations.get(i).setMessage(AppConstants.FAIL_MSG);
                }
                Thread.sleep(5000);
            }
        }





        return new ResponseEntity<>(routeLocations,HttpStatus.OK);


    }

}
