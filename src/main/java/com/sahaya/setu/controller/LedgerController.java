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
@CrossOrigin(origins = "*")
public class LedgerController {
    @Autowired
    private ShgGroupRepository groupRepo;

    @Autowired
    private MemberRepository memberRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    // 1. Fetch the "Total Amount of SHG"
    @GetMapping("/group/{groupId}/summary")
    public ResponseEntity<Map<String, Object>> getGroupSummary(@PathVariable Long groupId) {

        // 1. Find the group
        com.sahaya.setu.model.ShgGroup group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // 2. Fetch ALL transactions belonging to members of this specific group
        java.util.List<com.sahaya.setu.model.Transaction> allTransactions = transactionRepo.findAll().stream()
                .filter(t -> t.getMember() != null && t.getMember().getShgGroup() != null)
                .filter(t -> t.getMember().getShgGroup().getId().equals(groupId))
                .collect(java.util.stream.Collectors.toList());

        // 3. Dynamically calculate the true total money (Sum of all DEPOSITS)
        double dynamicTotal = allTransactions.stream()
                .filter(t -> t.getTransactionType() != null && t.getTransactionType().contains("DEPOSIT"))
                .mapToDouble(com.sahaya.setu.model.Transaction::getAmount)
                .sum();

        // 4. Auto-correct the database so the Group wallet is always in sync!
        group.setTotalCorpus(dynamicTotal);
        groupRepo.save(group);

        // 5. Send the perfect math back to React
        Map<String, Object> response = new HashMap<>();
        response.put("totalCorpus", dynamicTotal);
        response.put("shgName", group.getShgName());

        return ResponseEntity.ok(response);
    }

    // 2. The "Risk Ratio" Engine
    @GetMapping("/group/{groupId}/risk-report")
    public ResponseEntity<List<Map<String, Object>>> getRiskReport(@PathVariable Long groupId) {
        ShgGroup group = groupRepo.findById(groupId).orElseThrow();
        List<Member> members = memberRepo.findAll().stream()
                .filter(m -> m.getShgGroup().getId().equals(groupId))
                .collect(Collectors.toList());

        // Calculate risk per member based on total group corpus
        Double totalGroupMoney = group.getTotalCorpus() == 0 ? 1.0 : group.getTotalCorpus(); // Prevent divide by zero

        List<Map<String, Object>> riskReport = members.stream().map(member -> {
            Map<String, Object> report = new HashMap<>();
            report.put("memberName", member.getFullName());

            // In Phase 2, this will query the Loan table. For now, we mock the active loan
            Double activeLoan = 0.0; // To be replaced with actual loan query
            Double riskPercentage = (activeLoan / totalGroupMoney) * 100;

            report.put("activeLoan", activeLoan);
            report.put("riskPercentage", Math.round(riskPercentage) + "%");

            if (riskPercentage > 100) report.put("riskLevel", "HIGH");
            else if (riskPercentage > 50) report.put("riskLevel", "MEDIUM");
            else report.put("riskLevel", "LOW");

            return report;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(riskReport);
    }

    // 3. Fetch all Group Expenses/Income
    @GetMapping("/group/{groupId}/expenses")
    public ResponseEntity<List<Transaction>> getGroupExpenses(@PathVariable Long groupId) {

        List<Transaction> expenses = transactionRepo.findAll().stream()
                .filter(t -> t.getShgGroup() != null && t.getShgGroup().getId().equals(groupId))
                .filter(t -> t.getTransactionType().equals("GROUP_EXPENSE") || t.getTransactionType().equals("GROUP_INCOME"))
                .collect(Collectors.toList());

        return ResponseEntity.ok(expenses);
    }

    // 4. Fetch Recent Transactions for the Visual Ledger
    @GetMapping("/group/{groupId}/transactions")
    public ResponseEntity<List<Map<String, Object>>> getRecentTransactions(@PathVariable Long groupId) {

        // Fetch all transactions for this group, sorted newest first
        List<Map<String, Object>> formattedTransactions = transactionRepo.findAll().stream()
                .filter(t -> t.getShgGroup() != null && t.getShgGroup().getId().equals(groupId))
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate())) // Sort newest first
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", t.getId());
                    // If it's a member deposit, show their name. If it's an expense, show the description.
                    map.put("name", t.getMember() != null ? t.getMember().getFullName() : (t.getDescription() != null ? t.getDescription() : "Group Transaction"));
                    map.put("type", t.getTransactionType());
                    map.put("amount", t.getAmount());
                    map.put("date", t.getTransactionDate().toLocalDate().toString()); // Send a clean date
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(formattedTransactions);
    }
}
