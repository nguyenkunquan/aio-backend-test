package org.example.aiodataservice.application.base;

import org.example.aiodataservice.application.dtos.MyResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

//public class BaseResponse<T> {
//    protected ResponseEntity<?> getResponseEntity(List<String> errors, String message, T data) {
//        MyResponse myResponse = getResponse(errors, message, data);
//        return ResponseEntity.status(myResponse.getStatusCode()).body(myResponse);
//    }
//
//    private MyResponse getResponse(List<String> errors, String message, T data) {
//        MyResponse myResponse = new MyResponse(errors, message, data);
//        return myResponse;
//    }
//}
