package com.sahaya.setu.controller;

import com.sahaya.setu.model.Member;
import com.sahaya.setu.model.ShgGroup;
import com.sahaya.setu.model.Transaction;
import com.sahaya.setu.repository.LoanRepository;
import com.sahaya.setu.repository.MemberRepository;
import com.sahaya.setu.repository.TransactionRepository;
import com.sahaya.setu.repository.ShgGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "http://localhost:3000") // Secure CORS for React
public class MemberController {

    @Autowired
    private MemberRepository memberRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private LoanRepository loanRepo;

    @Autowired
    private ShgGroupRepository shgGroupRepo;

    // ==========================================
    // 1. ADD NEW MEMBER TO SPECIFIC GROUP
    // ==========================================
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addMember(@RequestBody Map<String, String> payload) {
        String fullName = payload.get("fullName");
        String mobileNumber = payload.get("mobileNumber");
        String password = payload.get("password");
        String groupIdStr = payload.get("groupId");

        if (memberRepo.findByMobileNumber(mobileNumber).isPresent()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "This mobile number is already registered.");
            return ResponseEntity.badRequest().body(error);
        }

        Member newMember = new Member();
        newMember.setFullName(fullName);
        newMember.setMobileNumber(mobileNumber);
        newMember.setPassword(password);
        newMember.setRole(Member.Role.MEMBER);

        newMember.setCredibilityScore(300); // Updated to match our new baseline
        newMember.setTotalSavedShare(0.0);
        newMember.setTotalLoanOutstanding(0.0); // Initialize new field

        if (groupIdStr != null) {
            ShgGroup group = shgGroupRepo.findById(Long.parseLong(groupIdStr))
                    .orElseThrow(() -> new RuntimeException("Group not found"));
            newMember.setShgGroup(group);
        }

        memberRepo.save(newMember);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", fullName + " has been added successfully!");
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 2. GET SAFE MEMBER PROFILE (OPTIMIZED WITH FINTECH DOMAIN)
    // ==========================================
    @GetMapping("/{mobileNumber}")
    public ResponseEntity<?> getMemberProfile(@PathVariable String mobileNumber) {

        // Find the member
        Member member = memberRepo.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // Fetch only their transactions safely
        List<Transaction> history = transactionRepo.findAll().stream()
                .filter(t -> t.getMember() != null && t.getMember().getId().equals(member.getId()))
                .collect(Collectors.toList());

        // Build a SAFE, clean package to send to React.
        // Notice how we use the direct Database values now instead of calculating them manually!
        Map<String, Object> safeProfile = new HashMap<>();
        safeProfile.put("fullName", member.getFullName());
        safeProfile.put("mobileNumber", member.getMobileNumber());
        safeProfile.put("credibilityScore", member.getCredibilityScore() != null ? member.getCredibilityScore() : 300);
        safeProfile.put("totalSaved", member.getTotalSavedShare());
        safeProfile.put("loanOutstanding", member.getTotalLoanOutstanding());

        // Clean up the transaction history using the NEW getter methods!
        List<Map<String, Object>> safeHistory = history.stream().map(t -> {
            Map<String, Object> txData = new HashMap<>();
            txData.put("amount", t.getTotalAmount()); // Fixed
            txData.put("type", t.getType().name());   // Fixed (Enum to String)
            txData.put("date", t.getTimestamp() != null ? t.getTimestamp().toLocalDate().toString() : "Today"); // Fixed
            return txData;
        }).collect(Collectors.toList());

        safeProfile.put("history", safeHistory);

        return ResponseEntity.ok(safeProfile);
    }
}