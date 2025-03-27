package com.kafka_impl.service.producer_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka_impl.service.producer_service.config.AppConstants;
import com.kafka_impl.service.producer_service.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private Logger logger = LoggerFactory.getLogger(KafkaService.class);

    private ObjectMapper objectMapper = new ObjectMapper();
    public boolean updateLocation(Location location){
        try{
            String jsonString = objectMapper.writeValueAsString(location);
            this.kafkaTemplate.send(AppConstants.LOCATION_TOPIC_NAME, location.getId(),jsonString);
            logger.info("Location Update Produced");
            return true;
        }
        catch(Exception e){
            logger.info("Exception while Location Update");
            return false;
        }
    }

}
