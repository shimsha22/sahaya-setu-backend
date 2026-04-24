package com.sahaya.setu.service;

import com.sahaya.setu.model.Member;
import com.sahaya.setu.model.Transaction;
import com.sahaya.setu.repository.MemberRepository;
import com.sahaya.setu.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VoiceCommandService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MemberRepository memberRepository;

    // We inject our new Math Engine!
    @Autowired
    private TransactionService transactionService;

    public Transaction processAndSaveCommand(String text) {
        String lowerText = text.toLowerCase();
        Member matchedMember = null;

        // 1. Identify the Member from the Database
        List<Member> allMembers = memberRepository.findAll();
        for (Member member : allMembers) {
            String firstName = member.getFullName().split(" ")[0].toLowerCase();
            if (lowerText.contains(firstName)) {
                matchedMember = member;
                break; // Found the person, stop searching
            }
        }

        if (matchedMember == null) {
            throw new RuntimeException("Could not recognize a member name in the audio.");
        }

        // 2. Extract the Amount
        double amount = 0.0;
        Matcher matcher = Pattern.compile("\\d+").matcher(lowerText);
        if (matcher.find()) {
            amount = Double.parseDouble(matcher.group());
        } else {
            throw new RuntimeException("Could not detect an amount in the audio.");
        }

        // 3. Identify the Intent and route it to the Financial Engine
        if (lowerText.contains("save") || lowerText.contains("jama") || lowerText.contains("deposit")) {
            // This guarantees the Group's Available Balance and Wealth update correctly!
            return transactionService.depositSavings(matchedMember.getId(), amount);
        } else {
            // For other intents, we build the entity using our new Upgraded Fields
            Transaction transaction = new Transaction();
            transaction.setMember(matchedMember);
            transaction.setGroup(matchedMember.getShgGroup()); // Upgraded to 'setGroup'
            transaction.setTotalAmount(amount); // Upgraded to 'setTotalAmount'
            transaction.setTimestamp(LocalDateTime.now());

            if (lowerText.contains("withdraw") || lowerText.contains("nikalo")) {
                // Upgraded to strict Enum
                transaction.setType(Transaction.TransactionType.WITHDRAWAL);
            } else {
                // Fallback
                transaction.setType(Transaction.TransactionType.SAVINGS_DEPOSIT);
            }

            return transactionRepository.save(transaction);
        }
    }
}