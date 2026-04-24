package com.sahaya.setu.service;
import com.sahaya.setu.model.*;
import com.sahaya.setu.repository.*; // Assuming you have standard JpaRepositories for your models
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class TransactionService {
    @Autowired
    private ShgGroupRepository groupRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * 1. SAVINGS DEPOSIT: Members put their monthly savings into the pool.
     */
    @Transactional
    public Transaction depositSavings(Long memberId, Double amount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        ShgGroup group = member.getShgGroup();

        // 1. Update Member's total saved
        member.setTotalSavedShare(member.getTotalSavedShare() + amount);

        // 2. Update Group's Pool (Both liquid cash and total wealth go up)
        group.setAvailableBalance(group.getAvailableBalance() + amount);
        group.setTotalGroupWealth(group.getTotalGroupWealth() + amount);

        // 3. Record the transaction
        Transaction transaction = new Transaction();
        transaction.setMember(member);
        transaction.setGroup(group);
        transaction.setType(Transaction.TransactionType.SAVINGS_DEPOSIT);
        transaction.setTotalAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());

        memberRepository.save(member);
        groupRepository.save(group);
        return transactionRepository.save(transaction);
    }

    /**
     * 2. LOAN DISBURSEMENT: Group gives money from the pool to a member.
     */
    @Transactional
    public Loan disburseLoan(Long memberId, Double principalRequested, Double interestRate) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        ShgGroup group = member.getShgGroup();

        // STRICT CHECK: Does the group actually have enough liquid cash?
        if (group.getAvailableBalance() < principalRequested) {
            throw new RuntimeException("Insufficient funds in the SHG Pool for this loan!");
        }

        // 1. Create the new Loan
        Loan loan = new Loan();
        loan.setMember(member);
        loan.setShgGroup(group);
        loan.setPrincipalAmount(principalRequested);
        loan.setOutstandingBalance(principalRequested);
        loan.setInterestRate(interestRate);
        loan.setStatus(Loan.LoanStatus.ACTIVE);
        loan.setDisbursementDate(LocalDate.now());

        // 2. Update the Member's outstanding debt
        member.setTotalLoanOutstanding(member.getTotalLoanOutstanding() + principalRequested);

        // 3. Update the Group's liquid cash (Wealth stays the same, cash just turns into debt)
        group.setAvailableBalance(group.getAvailableBalance() - principalRequested);

        // 4. Record the transaction
        Transaction transaction = new Transaction();
        transaction.setMember(member);
        transaction.setGroup(group);
        transaction.setType(Transaction.TransactionType.LOAN_DISBURSEMENT);
        transaction.setTotalAmount(principalRequested);
        transaction.setTimestamp(LocalDateTime.now());

        loanRepository.save(loan);
        memberRepository.save(member);
        groupRepository.save(group);
        transactionRepository.save(transaction);

        return loan;
    }

    /**
     * 3. LOAN REPAYMENT: The core SHG math engine. Splits payment into Interest and Principal.
     */
    @Transactional
    public Transaction repayLoan(Long loanId, Double paymentAmount) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getStatus() == Loan.LoanStatus.CLEARED) {
            throw new RuntimeException("This loan is already fully paid off.");
        }

        Member member = loan.getMember();
        ShgGroup group = loan.getShgGroup();


        double interestOwed = loan.getOutstandingBalance() * (loan.getInterestRate() / 100.0);

        if (paymentAmount < interestOwed) {
            throw new RuntimeException("Payment of " + paymentAmount + " is too small. Must cover at least the interest of " + interestOwed);
        }

        // MATH STEP 2: Split the payment
        double principalPaid = paymentAmount - interestOwed;

        // Prevent over-paying the loan
        if (principalPaid > loan.getOutstandingBalance()) {
            principalPaid = loan.getOutstandingBalance();
            paymentAmount = principalPaid + interestOwed; // Adjust total payment so they don't lose money
        }

        // 1. Update the Loan balance
        loan.setOutstandingBalance(loan.getOutstandingBalance() - principalPaid);
        if (loan.getOutstandingBalance() <= 0) {
            loan.setStatus(Loan.LoanStatus.CLEARED);
        }

        // 2. Update Member's profile
        member.setTotalLoanOutstanding(member.getTotalLoanOutstanding() - principalPaid);

        // 3. Update Group Pool (Liquid cash goes up by total payment. Total Wealth goes up by the interest profit!)
        group.setAvailableBalance(group.getAvailableBalance() + paymentAmount);
        group.setTotalGroupWealth(group.getTotalGroupWealth() + interestOwed);

        // 4. Record the highly detailed transaction
        Transaction transaction = new Transaction();
        transaction.setMember(member);
        transaction.setGroup(group);
        transaction.setRelatedLoan(loan);
        transaction.setType(Transaction.TransactionType.LOAN_REPAYMENT);
        transaction.setTotalAmount(paymentAmount);
        transaction.setPrincipalPortion(principalPaid);
        transaction.setInterestPortion(interestOwed);
        transaction.setTimestamp(LocalDateTime.now());

        loanRepository.save(loan);
        memberRepository.save(member);
        groupRepository.save(group);
        return transactionRepository.save(transaction);
    }
}
