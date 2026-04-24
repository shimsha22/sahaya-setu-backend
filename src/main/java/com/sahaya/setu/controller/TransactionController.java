package com.sahaya.setu.controller;

import com.sahaya.setu.model.Loan;
import com.sahaya.setu.model.Transaction;
import com.sahaya.setu.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * POST /api/transactions/deposit
     * React payload: { "memberId": 1, "amount": 500 }
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> depositSavings(@RequestBody Map<String, Object> payload) {
        try {
            // We extract the numbers React sends us in the JSON body
            Long memberId = Long.valueOf(payload.get("memberId").toString());
            Double amount = Double.valueOf(payload.get("amount").toString());

            // Hand it to our secure engine
            Transaction transaction = transactionService.depositSavings(memberId, amount);

            // Send the 200 OK receipt back to React
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            // If the math fails (e.g., negative money), send a 400 Error back to React
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/transactions/loan/disburse
     * React payload: { "memberId": 1, "principalRequested": 5000, "interestRate": 2.0 }
     */
    @PostMapping("/loan/disburse")
    public ResponseEntity<?> disburseLoan(@RequestBody Map<String, Object> payload) {
        try {
            Long memberId = Long.valueOf(payload.get("memberId").toString());
            Double principalRequested = Double.valueOf(payload.get("principalRequested").toString());
            Double interestRate = Double.valueOf(payload.get("interestRate").toString());

            Loan loan = transactionService.disburseLoan(memberId, principalRequested, interestRate);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/transactions/loan/repay

     */
    @PostMapping("/loan/repay")
    public ResponseEntity<?> repayLoan(@RequestBody Map<String, Object> payload) {
        try {
            Long loanId = Long.valueOf(payload.get("loanId").toString());
            Double paymentAmount = Double.valueOf(payload.get("paymentAmount").toString());

            Transaction transaction = transactionService.repayLoan(loanId, paymentAmount);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}