package com.selflearning.jwtconsumer.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selflearning.jwtconsumer.models.RegistrationUser;
import com.selflearning.jwtconsumer.models.ResponseToken;
import com.selflearning.jwtconsumer.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;



@RestController
public class JWTConsumerController {

    @Autowired
    RestTemplate restTemplate;

    private final static String REGISTRTION_URL = "http://localhost:8080/register";
    private final static String AUTHENTICTION_URL = "http://localhost:8080/authenticate";
    private final static String HELLO_URL = "http://localhost:8080/hello";

    private String getBody(final User user)throws JsonProcessingException{
        return new ObjectMapper().writeValueAsString(user);
    }

    private HttpHeaders getHeaders(){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        return httpHeaders;
    }

    private User getAuthenticationUser(){
        User user = new User();
        user.setUsername("mahesh");
        user.setPassword("Test1234$");
        return user;
    }

    private RegistrationUser getRegistrationUser(){
        RegistrationUser registrationUser = new RegistrationUser();
        registrationUser.setUsername("mahesh");
        registrationUser.setPassword("Test1234$");
        registrationUser.setRole("ROLE_ADMIN");
        return registrationUser;
    }

    @GetMapping ("/getResponse")
    public String getResponse() throws JsonProcessingException{

        String response = null;
        //Create User registration Object
        RegistrationUser registrationUser = getRegistrationUser();
        //Convert the user registration object to JSon
        String registrationBody = getBody(registrationUser);
        //Create headers specifying that it is JSON Request
        HttpHeaders registrationHeaders = getHeaders();
        HttpEntity<String> registrationEntity = new HttpEntity<String>(registrationBody, registrationHeaders);

        try{
            //Register Uer
            ResponseEntity<String> registrationResponse = restTemplate.exchange(REGISTRTION_URL, HttpMethod.POST, registrationEntity, String.class);

            //If Registration is successful
            if(registrationResponse.getStatusCode().equals(HttpStatus.OK)){
                //Create User Authentication object
                User authenticationUser = getAuthenticationUser();

                //convert the user authentication object to JSon
                String authenticationBody = getBody(authenticationUser);

                //create heders specifying that it is JSon request
                HttpHeaders authenticationHeaders = getHeaders();
                HttpEntity<String> authenticationEntity = new HttpEntity<String>(authenticationBody, authenticationHeaders);

                ResponseEntity<ResponseToken> authenticationResponse = restTemplate.exchange(AUTHENTICTION_URL, HttpMethod.POST, authenticationEntity, ResponseToken.class);

                //If authentication is successful
                if(authenticationResponse.getStatusCode().equals(HttpStatus.OK)){
                    String token = "Bearer " + authenticationResponse.getBody().getToken();
                    HttpHeaders httpHeaders = getHeaders();
                    httpHeaders.set("Authorization", token);

                    HttpEntity<String> jwtEntity = new HttpEntity<String>(httpHeaders);
                    //Use token to get response

                    ResponseEntity<String> helloResponse = restTemplate.exchange(HELLO_URL, HttpMethod.GET, jwtEntity, String.class);
                    if(helloResponse.getStatusCode().equals(HttpStatus.OK)){
                        response = helloResponse.getBody();
                    }
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }
        return response;
    }
}
