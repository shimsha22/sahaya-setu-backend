package com.sahaya.setu.service;

import com.sahaya.setu.model.Member;
import com.sahaya.setu.model.Transaction;
import com.sahaya.setu.repository.MemberRepository;
import com.sahaya.setu.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VoiceCommandService {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MemberRepository memberRepository;

    public Transaction processAndSaveCommand(String text) {
        Transaction transaction = new Transaction();
        transaction.setVoiceCommandRaw(text);

        String lowerText = text.toLowerCase();

        // 1. Identify the Member from the Database
        List<Member> allMembers = memberRepository.findAll();
        for (Member member : allMembers) {
            String firstName = member.getFullName().split(" ")[0].toLowerCase();
            if (lowerText.contains(firstName)) {
                transaction.setMember(member);
                transaction.setShgGroup(member.getShgGroup());
                break; // Found the person, stop searching
            }
        }

        // 2. Identify the Intent
        if (lowerText.contains("save") || lowerText.contains("jama") || lowerText.contains("deposit")) {
            transaction.setTransactionType("SAVINGS_DEPOSIT");
        } else if (lowerText.contains("withdraw") || lowerText.contains("nikalo") || lowerText.contains("loan")) {
            transaction.setTransactionType("LOAN_REPAYMENT");
        } else {
            transaction.setTransactionType("GROUP_EXPENSE");
        }

        // 3. Extract the Amount
        Matcher matcher = Pattern.compile("\\d+").matcher(lowerText);
        if (matcher.find()) {
            transaction.setAmount(Double.parseDouble(matcher.group()));
        } else {
            transaction.setAmount(0.0);
        }

        return transactionRepository.save(transaction);
    }
}
