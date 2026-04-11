package com.sahaya.setu.controller;

import com.sahaya.setu.model.Transaction;
import com.sahaya.setu.service.VoiceCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class VoiceController {
    @Autowired
    private VoiceCommandService voiceCommandService;

    // This endpoint listens for POST requests at http://localhost:8080/api/voice
    @PostMapping("/voice")
    public ResponseEntity<Transaction> handleVoiceCommand(@RequestBody Map<String, String> payload) {

        // Extract the spoken text from the incoming request
        String commandText = payload.get("command");

        // Pass it to our "Brain" (the Service) to figure out who, what, and how much
        Transaction savedTransaction = voiceCommandService.processAndSaveCommand(commandText);

        // Send the saved transaction back as a success response
        return ResponseEntity.ok(savedTransaction);
    }
}
