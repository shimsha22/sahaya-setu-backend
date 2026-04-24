package com.sahaya.setu.controller;

import com.sahaya.setu.model.Member;
import com.sahaya.setu.model.ShgGroup;
import com.sahaya.setu.repository.MemberRepository;
import com.sahaya.setu.repository.ShgGroupRepository;
import com.sahaya.setu.security.JwtUtil;
import com.sahaya.setu.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // Secure CORS for React
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ShgGroupRepository shgGroupRepo;

    // --- 1. REGISTRATION FLOW (OTP Required) ---

    @PostMapping("/register/request-otp")
    public ResponseEntity<Map<String, String>> requestRegistrationOtp(@RequestBody Map<String, String> payload) {
        String mobile = payload.get("mobileNumber");
        String message = authService.generateAndSendOtp(mobile);

        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/confirm")
    public ResponseEntity<Map<String, Object>> confirmRegistration(@RequestBody Map<String, String> payload) {
        String mobile = payload.get("mobileNumber");
        String otp = payload.get("otp");
        String password = payload.get("password");
        String fullName = payload.get("fullName");

        // 1. Verify the OTP
        if (!authService.verifyOtp(mobile, otp)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid OTP"));
        }

        // 2. Create their SHG Group first (UPGRADED FIELDS)
        ShgGroup newGroup = new ShgGroup();
        newGroup.setShgName(fullName + "'s SHG");
        newGroup.setShgCode("SHG-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase());

        // --- THE FIX: Using the new strict financial fields ---
        newGroup.setAvailableBalance(0.0);
        newGroup.setTotalGroupWealth(0.0);

        newGroup.setBaseMonthlySaving(500.0);
        newGroup = shgGroupRepo.save(newGroup);

        // 3. Save the new member and link them to the group
        Member newMember = new Member();
        newMember.setMobileNumber(mobile);
        newMember.setPassword(password);
        newMember.setFullName(fullName);
        newMember.setRole(Member.Role.PRESIDENT);
        newMember.setShgGroup(newGroup);
        memberRepo.save(newMember);

        return ResponseEntity.ok(Map.of("success", true, "message", "Registration complete! You can now log in."));
    }

    // --- 2. LOGIN FLOW (Password Required, NO OTP) ---

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> payload) {
        String mobile = payload.get("mobileNumber");
        String password = payload.get("password");

        Optional<Member> memberOpt = memberRepo.findByMobileNumber(mobile);

        if (memberOpt.isPresent() && memberOpt.get().getPassword().equals(password)) {
            Member member = memberOpt.get();
            String token = jwtUtil.generateToken(mobile);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("role", member.getRole().toString());
            response.put("fullName", member.getFullName());

            if (member.getShgGroup() != null) {
                response.put("groupId", member.getShgGroup().getId());
            }

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid credentials"));
    }
}