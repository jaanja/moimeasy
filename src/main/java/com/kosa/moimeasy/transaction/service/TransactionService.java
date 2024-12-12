package com.kosa.moimeasy.transaction.service;

import static com.kosa.moimeasy.transaction.type.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.kosa.moimeasy.transaction.type.ErrorCode.BALANCE_NOT_ENOUGH;
import static com.kosa.moimeasy.transaction.type.ErrorCode.INVALID_DATE;
import static com.kosa.moimeasy.transaction.type.ErrorCode.INVALID_DATE_RANGE;
import static com.kosa.moimeasy.transaction.type.ErrorCode.NOT_EQUAL_ID_AND_ACCOUNT_NUMBER;
import static com.kosa.moimeasy.transaction.type.ErrorCode.RECEIVED_ACCOUNT_NOT_FOUND;
import static com.kosa.moimeasy.transaction.type.ErrorCode.SENT_ACCOUNT_NOT_FOUND;
import static com.kosa.moimeasy.transaction.type.ErrorCode.TOKEN_NOT_MATCH_USER;

import com.kosa.moimeasy.moeim.entity.Moeim;
import com.kosa.moimeasy.moeim.repository.MoeimRepository;
import com.kosa.moimeasy.security.provider.JwtTokenProvider;
import com.kosa.moimeasy.transaction.dto.*;
import com.kosa.moimeasy.transaction.entity.TransactionEntity;
import com.kosa.moimeasy.transaction.exception.CustomException;
import com.kosa.moimeasy.transaction.repository.TransactionRepository;
import com.kosa.moimeasy.transaction.type.ErrorCode;
import com.kosa.moimeasy.transaction.type.Transaction;
import com.kosa.moimeasy.user.entity.User;
import com.kosa.moimeasy.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final UserRepository userRepository;
    private final MoeimRepository moeimRepository;
    private final TransactionRepository transactionRepository;

    private final JwtTokenProvider tokenProvider;

    // 입금
    @Transactional
    public DepositDto.Response deposit(DepositDto.Request request) {
        // 계좌번호 존재(삭제 여부) 확인
        Moeim moeim = moeimRepository
            .findByAccountNumber(request.getAccountNumber())
            .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));

        // 계좌가 있다면 입금 금액만큼 잔액 변경
        moeim.setAmount(moeim.getAmount() + request.getAmount());

        // 거래 내역 테이블에 거래추가
        transactionRepository.save(
            TransactionEntity.builder()
                .moeimAccount(moeim)
                .transactionType(Transaction.DEPOSIT)
                .amount(request.getAmount())
                .depositName(request.getDepositName())
                .build()
        );

        return DepositDto.Response.builder()
            .accountNumber(request.getAccountNumber())
            .depositName(request.getDepositName())
            .amount(request.getAmount())
            .transacted_at(LocalDateTime.now())
            .build();
    }

    // 출금
    @Transactional
    public WithdrawDto.Response withdraw(String token, WithdrawDto.Request request) {
        // 출금 요청한 계좌의 존재 여부 확인
        Long tokenUserId = tokenProvider.getUserIdFromJWT(token);
        Moeim moeim = moeimRepository.findByAccountNumber(
                request.getAccountNumber())
            .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));

        // 토큰의 사용자와 출금 요청 계좌의 소유주 일치 여부 확인
        if (!Objects.equals(tokenUserId, moeim.getUser().getUserId())) {
            throw new CustomException(TOKEN_NOT_MATCH_USER);
        }

        // (출금 요청 금액 > 잔액)의 경우 예외 발생
        if (request.getAmount() > moeim.getAmount()) {
            throw new CustomException(BALANCE_NOT_ENOUGH);
        }

        // 출금 요청한 금액만큼 잔액 변경
        moeim.setAmount(moeim.getAmount() - request.getAmount());

        // 거래 내역 테이블에 거래 추가
        transactionRepository.save(
            TransactionEntity.builder()
                .moeimAccount(moeim)
                .transactionType(Transaction.WITHDRAW)
                .amount(request.getAmount())
                .withdrawName(request.getWithdrawName())
                .build()
        );

        return WithdrawDto.Response.builder()
            .accountNumber(request.getAccountNumber())
            .withdrawName(request.getWithdrawName())
            .amount(request.getAmount())
            .transacted_at(LocalDateTime.now())
            .build();
    }


    // 송금
    @Transactional
    public RemittanceDto.Response remittance(String token, RemittanceDto.Request request) {
        User sentAccount = userRepository.findByAccountNumber(
                request.getSentAccountNumber())
            .orElseThrow(() -> new CustomException(SENT_ACCOUNT_NOT_FOUND));

        Moeim receivedAccount = moeimRepository.findByAccountNumber(
                request.getReceivedAccountNumber())
            .orElseThrow(() -> new CustomException(RECEIVED_ACCOUNT_NOT_FOUND));

        // 토큰의 사용자와 보내는 계좌의 사용자 확인
        Long tokenUserId = tokenProvider.getUserIdFromJWT(token);

        if (!Objects.equals(tokenUserId, sentAccount.getUserId())) {
            throw new CustomException(TOKEN_NOT_MATCH_USER);
        }

        // (송금 요청 금액 > 보내는 계좌의 잔액)의 경우 예외 발생
        if (request.getAmount() > sentAccount.getAmount()) {
            throw new CustomException(BALANCE_NOT_ENOUGH);
        }

        // 보내는 계좌, 받는 계좌의 잔액 변경
        sentAccount.setAmount(sentAccount.getAmount() - request.getAmount());
        receivedAccount.setAmount(receivedAccount.getAmount() + request.getAmount());

        // 거래 테이블에 거래 저장
        transactionRepository.save(
            TransactionEntity.builder()
                    .moeimAccount(receivedAccount)
                .userAccount(sentAccount)
                .transactionType(Transaction.REMITTANCE)
                .amount(request.getAmount())
                .receivedName(receivedAccount.getMoeimName())
                .receivedAccount(request.getReceivedAccountNumber())
                .build()
        );

        return RemittanceDto.Response.builder()
            .sentAccountNumber(request.getSentAccountNumber())
            .receivedAccountNumber(request.getReceivedAccountNumber())
            .receivedName(receivedAccount.getMoeimName())
            .amount(request.getAmount())
            .build();
    }

    /**
     * 거래 내역 조회_23.08.04
     *
     * @apiNote 삭제된 계좌는 거래 내역 조회 불가
     * @apiNote 삭제되지 않은 계좌의 거래 내역에 포함된 삭제된 계좌와의 거래는 표시
     */
    @Transactional
    public TransactionListDto.Response getTransactionList
    (
        String token, TransactionListDto.Request request
    ) {
        // 조회 계좌 존재/삭제 여부 확인
        Moeim moeim = getValidmoeim(request.getAccountNumber());

        // 토큰의 사용자 id와 거래내역을 조회할 계좌의 userId 확인
        if (!Objects.equals(tokenProvider.getUserIdFromJWT(token), moeim.getUser().getUserId())) {
            throw new CustomException(TOKEN_NOT_MATCH_USER);
        }

        // 계좌 id 와 계좌번호의 계좌 id 일치 여부 확인
        TransactionEntity transactionEntity = transactionRepository.findById(request.getAccountId())
            .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));
        if (!request.getAccountNumber().equals(transactionEntity.getMoeimAccount().getAccountNumber())) {
            throw new CustomException(NOT_EQUAL_ID_AND_ACCOUNT_NUMBER);
        }

        // 올바르게 요청된 날짜 형식: "yyyy-MM-dd"
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        LocalDate nowDate = LocalDate.now();
        int defaultDateRange = 7;
        int maxDateRange = 7;

        // 시작 날짜와 끝 날짜가 null 인 경우 (조회일 포함 일주일 내역 반환)
        if (startDate == null && endDate == null) {
            LocalDate weekAgoDate = nowDate.minusDays(defaultDateRange - 1);

            return getTransactionListResponse(request.getAccountId(), weekAgoDate, nowDate);
        }

        // 시작 날짜와 끝 날짜 둘 중 하나만 null 로 보낸 경우
        if (startDate == null || endDate == null) {
            throw new CustomException(INVALID_DATE);
        }

        // ======== 두 날짜 다 null 이 아닌 경우들 ========

        // 시작 날짜가 끝 날짜를 초과한 경우
        if (startDate.isAfter(endDate)) {
            throw new CustomException(INVALID_DATE);
        }

        // 끝 날짜가 조회 당일 날짜를 초과한 경우
        if (endDate.isAfter(nowDate)) {
            throw new CustomException(INVALID_DATE);
        }

        // 조회 기간이 최대 조회 기간을 넘을 경우
        int betweenDays = (int) ChronoUnit.DAYS.between(startDate, endDate);
        if (betweenDays + 1 > maxDateRange) {
            throw new CustomException(INVALID_DATE_RANGE);
        }

        // 두 날짜 전부 제대로 조회한 경우
        return getTransactionListResponse(request.getAccountId(), startDate, endDate);
    }

    // 토큰에서 추출한 사용자와 요청 객체에서 추출한 사용자가 일치한지
    // 확인하는 private 메소드 만들기!

    /**
     * 계좌 존재/삭제 여부 확인_23.08.03
     */
    private Moeim getValidmoeim(String accountNumber) {
        Moeim moeim = moeimRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new CustomException(ACCOUNT_NOT_FOUND));

        return moeim;
    }

    /**
     * 거래 종류에 따라 거래 대상자 이름 반환_23.08.03
     *
     * @implNote 반환되는 이름 종류: 출금자명, 입금자명, 송금 받은 계좌 소유주명
     * @implNote getTransactionList 에서만 사용
     */
    private String getTransactionTargetName(TransactionEntity transaction) {
        switch (transaction.getTransactionType()) {
            case WITHDRAW -> {
                return transaction.getWithdrawName();
            }
            case DEPOSIT -> {
                return transaction.getDepositName();
            }
            case REMITTANCE -> {
                return transaction.getReceivedName();
            }
        }
        return ErrorCode.TRANSACTION_TYPE_NOT_FOUND.getDescription();
    }

    /**
     * 거래 내역 조회 Response Dto 반환_23.08.04
     *
     * @implNote getTransactionList 에서만 사용
     */
    private TransactionListDto.Response getTransactionListResponse(
        Long accountId, LocalDate startDate, LocalDate endDate
    ) {
        List<TransactionEntity> resultList = transactionRepository.findByAccountIdAndTransactedAtBetween(
            accountId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX)
        );
        return TransactionListDto.Response.builder()
            .transactionList(resultList.stream()
                .map(transaction ->
                    TransactionDto.builder()
                        .id(transaction.getId())
                        .transactionTargetName(getTransactionTargetName(transaction))
                        .amount(transaction.getAmount())
                        .type(transaction.getTransactionType())
                        .transactedAt(transaction.getTransactedAt())
                        .build()
                ).toList())
            .build();
    }
}