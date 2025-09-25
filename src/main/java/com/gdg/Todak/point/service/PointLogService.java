package com.gdg.Todak.point.service;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.dto.PointLogRequest;
import com.gdg.Todak.point.dto.PointLogResponse;
import com.gdg.Todak.point.dto.PointLogSearchRequest;
import com.gdg.Todak.point.entity.PointLog;
import com.gdg.Todak.point.repository.PointLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.gdg.Todak.common.exception.errors.PointError.LOCK_ERROR_LOG_UPLOAD_ERROR;
import static com.gdg.Todak.common.exception.errors.PointError.POINT_LOG_UPLOAD_ERROR;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PointLogService {

    private final PointLogRepository pointLogRepository;
    private final MemberRepository memberRepository;
    @Value("${file.path}")
    private String uploadFolder;

    public Page<PointLogResponse> getPointLogList(String userId, PointLogSearchRequest request, Pageable pageable) {
        Page<PointLog> pointLogs = getPointLogs(
                userId,
                request.getPointType(),
                request.getPointStatus(),
                request.getStartDate(),
                request.getEndDate(),
                pageable
        );
        return pointLogs
                .map(pointLog -> new PointLogResponse(
                        pointLog.getPointType(),
                        pointLog.getPointStatus(),
                        pointLog.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        pointLog.getPoint()));
    }

    @Transactional
    public void createPointLog(PointLogRequest pointLogRequest) {
        PointLog pointLog = PointLog.builder()
                .member(pointLogRequest.member())
                .point(pointLogRequest.point())
                .pointType(pointLogRequest.pointType())
                .pointStatus(pointLogRequest.pointStatus())
                .build();

        pointLogRepository.save(pointLog);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
//                savePointLogToServer(pointLogRequest);
            }
        });
    }

    private void savePointLogToServer(PointLogRequest pointLogRequest) {
        String subDirectory = "pointLogs/" + pointLogRequest.member().getUserId();
        Path directoryPath = Paths.get(uploadFolder, subDirectory);
        Path logFilePath = directoryPath.resolve("logs.txt");

        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            String logEntry = String.format(
                    "[%s] <Status: %s> UserId: %s, Point: %d, Type: %s%n",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    pointLogRequest.pointStatus(),
                    pointLogRequest.member().getUserId(),
                    pointLogRequest.point(),
                    pointLogRequest.pointType()
            );

            Files.writeString(logFilePath, logEntry,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {
            throw new TodakException(POINT_LOG_UPLOAD_ERROR);
        }
    }

    public void saveLockErrorLogToServer(Member member, String message) {
        String subDirectory = "lockErrorLogs/" + member.getUserId();
        Path directoryPath = Paths.get(uploadFolder, subDirectory);
        Path logFilePath = directoryPath.resolve("lock-errors.txt");

        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            String logEntry = String.format(
                    "[%s] UserId: %s - %s%n",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    member.getUserId(),
                    message
            );

            Files.writeString(logFilePath, logEntry,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {
            throw new TodakException(LOCK_ERROR_LOG_UPLOAD_ERROR);
        }
    }

    private Page<PointLog> getPointLogs(String userId, String pointType, String pointStatus, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<PointLog> spec = Specification.where(null);

        if (userId != null && !userId.isBlank()) {
            spec = spec.and(PointLogSpecifications.hasUserId(userId));
        }
        if (pointType != null && !pointType.isBlank()) {
            spec = spec.and(PointLogSpecifications.hasPointType(PointType.valueOf(pointType)));
        }
        if (pointStatus != null && !pointStatus.isBlank()) {
            spec = spec.and(PointLogSpecifications.hasPointStatus(PointStatus.valueOf(pointStatus)));
        }
        if (startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);
            spec = spec.and(PointLogSpecifications.createdAtBetween(start, end));
        }

        return pointLogRepository.findAll(spec, pageable);
    }

    private class PointLogSpecifications {
        public static Specification<PointLog> hasUserId(String userId) {
            return (root, query, cb) -> cb.equal(root.get("member").get("userId"), userId);
        }

        public static Specification<PointLog> hasPointType(PointType pointType) {
            return (root, query, cb) -> cb.equal(root.get("pointType"), pointType);
        }

        public static Specification<PointLog> hasPointStatus(PointStatus pointStatus) {
            return (root, query, cb) -> cb.equal(root.get("pointStatus"), pointStatus);
        }

        public static Specification<PointLog> createdAtBetween(LocalDateTime start, LocalDateTime end) {
            return (root, query, cb) -> cb.between(root.get("createdAt"), start, end);
        }
    }
}
