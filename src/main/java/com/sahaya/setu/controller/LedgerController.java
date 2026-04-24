package com.sahaya.setu.controller;

import com.sahaya.setu.model.Member;
import com.sahaya.setu.model.ShgGroup;
import com.sahaya.setu.model.Transaction;
import com.sahaya.setu.repository.MemberRepository;
import com.sahaya.setu.repository.ShgGroupRepository;
import com.sahaya.setu.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ledger")
@CrossOrigin(origins = "http://localhost:3000") // Secure CORS for React
public class LedgerController {

    @Autowired
    private ShgGroupRepository groupRepo;

    @Autowired
    private MemberRepository memberRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    // 1. Fetch the "Total Amount of SHG" (Now instantly reads from our secure Domain Model)
    @GetMapping("/group/{groupId}/summary")
    public ResponseEntity<Map<String, Object>> getGroupSummary(@PathVariable Long groupId) {

        ShgGroup group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // We send both metrics to React so the Dashboard can show Liquid vs Total
        Map<String, Object> response = new HashMap<>();


        response.put("totalGroupWealth", group.getTotalGroupWealth());
        response.put("availableBalance", group.getAvailableBalance());
        response.put("shgName", group.getShgName());

        return ResponseEntity.ok(response);
    }

    // 2. The "Risk Ratio" Engine (UPGRADED with real Loan data!)
    @GetMapping("/group/{groupId}/risk-report")
    public ResponseEntity<List<Map<String, Object>>> getRiskReport(@PathVariable Long groupId) {
        ShgGroup group = groupRepo.findById(groupId).orElseThrow();

        List<Member> members = memberRepo.findAll().stream()
                .filter(m -> m.getShgGroup().getId().equals(groupId))
                .collect(Collectors.toList());

        // Calculate risk per member based on total group wealth
        Double totalGroupMoney = group.getTotalGroupWealth() == 0 ? 1.0 : group.getTotalGroupWealth(); // Prevent divide by zero

        List<Map<String, Object>> riskReport = members.stream().map(member -> {
            Map<String, Object> report = new HashMap<>();
            report.put("memberName", member.getFullName());

            // UPGRADE: Now uses actual live data from the Member profile!
            Double activeLoan = member.getTotalLoanOutstanding();
            Double riskPercentage = (activeLoan / totalGroupMoney) * 100;

            report.put("activeLoan", activeLoan);
            report.put("riskPercentage", Math.round(riskPercentage) + "%");

            if (riskPercentage > 80) report.put("riskLevel", "HIGH");
            else if (riskPercentage > 40) report.put("riskLevel", "MEDIUM");
            else report.put("riskLevel", "LOW");

            return report;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(riskReport);
    }

    // 3. Fetch all Group Expenses/Income (Adapted for Enum Types)
    @GetMapping("/group/{groupId}/expenses")
    public ResponseEntity<List<Transaction>> getGroupExpenses(@PathVariable Long groupId) {

        // Filters for money leaving the group (Disbursements or Withdrawals)
        List<Transaction> expenses = transactionRepo.findAll().stream()
                .filter(t -> t.getGroup() != null && t.getGroup().getId().equals(groupId))
                .filter(t -> t.getType() == Transaction.TransactionType.LOAN_DISBURSEMENT ||
                        t.getType() == Transaction.TransactionType.WITHDRAWAL)
                .collect(Collectors.toList());

        return ResponseEntity.ok(expenses);
    }

    // 4. Fetch Recent Transactions for the Visual Ledger (Adapted for new Entity names)
    @GetMapping("/group/{groupId}/transactions")
    public ResponseEntity<List<Map<String, Object>>> getRecentTransactions(@PathVariable Long groupId) {

        // Fetch all transactions for this group, sorted newest first
        List<Map<String, Object>> formattedTransactions = transactionRepo.findAll().stream()
                .filter(t -> t.getGroup() != null && t.getGroup().getId().equals(groupId))
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp())) // Sort newest first
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", t.getId());
                    map.put("name", t.getMember() != null ? t.getMember().getFullName() : "Group System");
                    map.put("type", t.getType().name()); // Converts Enum to String for React
                    map.put("amount", t.getTotalAmount());
                    map.put("date", t.getTimestamp().toLocalDate().toString()); // Send a clean date
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(formattedTransactions);
    }
}