package com.aiart.platform.service.impl;

import com.aiart.platform.dto.PointDtos;
import com.aiart.platform.entity.PointAccount;
import com.aiart.platform.entity.PointTransaction;
import com.aiart.platform.entity.User;
import com.aiart.platform.exception.BusinessException;
import com.aiart.platform.exception.ErrorCode;
import com.aiart.platform.mapper.PointAccountMapper;
import com.aiart.platform.mapper.PointTransactionMapper;
import com.aiart.platform.mapper.UserMapper;
import com.aiart.platform.service.PointService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    private static final BigDecimal DAILY_CLAIM_AMOUNT = new BigDecimal("100.00");

    private final PointAccountMapper pointAccountMapper;
    private final PointTransactionMapper pointTransactionMapper;
    private final UserMapper userMapper;

    @Override
    public PointDtos.AccountView account(Long userId, int page, int size) {
        PointAccount account = ensureAccount(userId);
        Page<PointTransaction> result = pointTransactionMapper.selectPage(new Page<>(Math.max(1, page), Math.min(Math.max(1, size), 30)),
                Wrappers.<PointTransaction>lambdaQuery()
                        .eq(PointTransaction::getUserId, userId)
                        .orderByDesc(PointTransaction::getCreatedAt));
        List<PointDtos.TransactionView> transactions = result.getRecords().stream().map(this::transactionView).toList();
        return new PointDtos.AccountView(account.getBalance(), account.getFrozenBalance(), transactions);
    }

    @Override
    @Transactional
    public PointDtos.AccountView claimDaily(Long userId) {
        LocalDate today = LocalDate.now();
        Long count = pointTransactionMapper.selectCount(Wrappers.<PointTransaction>lambdaQuery()
                .eq(PointTransaction::getUserId, userId)
                .eq(PointTransaction::getDirection, "IN")
                .eq(PointTransaction::getReason, "DAILY_CLAIM")
                .ge(PointTransaction::getCreatedAt, today.atStartOfDay()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "今日积分已领取");
        }
        earn(userId, DAILY_CLAIM_AMOUNT, "DAILY_CLAIM", "POINT_ACCOUNT", userId);
        return account(userId, 1, 20);
    }

    @Override
    @Transactional
    public void spend(Long userId, BigDecimal amount, String reason, String referenceType, Long referenceId) {
        BigDecimal normalized = normalizePositive(amount);
        PointAccount account = ensureAccount(userId);
        if (account.getBalance().compareTo(normalized) < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "积分余额不足");
        }
        account.setBalance(account.getBalance().subtract(normalized));
        account.setUpdatedAt(LocalDateTime.now());
        pointAccountMapper.updateById(account);
        syncUserPointBalance(userId, account.getBalance());
        createTransaction(userId, normalized.negate(), "OUT", reason, referenceType, referenceId);
    }

    @Override
    @Transactional
    public void earn(Long userId, BigDecimal amount, String reason, String referenceType, Long referenceId) {
        BigDecimal normalized = normalizePositive(amount);
        PointAccount account = ensureAccount(userId);
        account.setBalance(account.getBalance().add(normalized));
        account.setUpdatedAt(LocalDateTime.now());
        pointAccountMapper.updateById(account);
        syncUserPointBalance(userId, account.getBalance());
        createTransaction(userId, normalized, "IN", reason, referenceType, referenceId);
    }

    private PointAccount ensureAccount(Long userId) {
        PointAccount account = pointAccountMapper.selectOne(Wrappers.<PointAccount>lambdaQuery()
                .eq(PointAccount::getUserId, userId));
        if (account != null) {
            return account;
        }
        LocalDateTime now = LocalDateTime.now();
        PointAccount created = new PointAccount();
        created.setUserId(userId);
        created.setBalance(BigDecimal.ZERO);
        created.setFrozenBalance(BigDecimal.ZERO);
        created.setCreatedAt(now);
        created.setUpdatedAt(now);
        pointAccountMapper.insert(created);
        return created;
    }

    private void createTransaction(Long userId, BigDecimal amount, String direction, String reason, String referenceType, Long referenceId) {
        PointTransaction transaction = new PointTransaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setDirection(direction);
        transaction.setReason(reason);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setCreatedAt(LocalDateTime.now());
        pointTransactionMapper.insert(transaction);
    }

    private void syncUserPointBalance(Long userId, BigDecimal balance) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        user.setPointBalance(balance);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private BigDecimal normalizePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "积分数量必须大于 0");
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private PointDtos.TransactionView transactionView(PointTransaction transaction) {
        return new PointDtos.TransactionView(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDirection(),
                transaction.getReason(),
                transaction.getReferenceType(),
                transaction.getReferenceId(),
                transaction.getCreatedAt());
    }
}
