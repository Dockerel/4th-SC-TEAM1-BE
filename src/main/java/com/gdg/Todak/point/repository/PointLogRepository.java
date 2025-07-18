package com.gdg.Todak.point.repository;

import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.point.PointStatus;
import com.gdg.Todak.point.PointType;
import com.gdg.Todak.point.entity.PointLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PointLogRepository extends JpaRepository<PointLog, Long> {
    Page<PointLog> findAllByMember(Member member, Pageable pageable);

    boolean existsByCreatedAtBetweenAndMemberAndPointTypeIn(Instant start, Instant end, Member member, List<PointType> pointTypes);

    boolean existsByMemberAndPointType(Member member, PointType pointType);

    List<PointLog> findAllByMember_UserId(String userId);

    List<PointLog> findAllByPointType(PointType pointType);

    List<PointLog> findAllByPointStatus(PointStatus pointStatus);

    List<PointLog> findAllByCreatedAtBetween(Instant start, Instant end);

    Page<PointLog> findAll(Specification<PointLog> spec, Pageable pageable);

    @Query("SELECT DISTINCT pl.member.id FROM PointLog pl WHERE pl.createdAt BETWEEN :start AND :end")
    List<Long> findMemberIdsWithActivityBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
                SELECT COALESCE(SUM(CASE WHEN pl.pointStatus = 'EARNED' THEN pl.point ELSE 0 END), 0) - 
                       COALESCE(SUM(CASE WHEN pl.pointStatus = 'CONSUMED' THEN pl.point ELSE 0 END), 0)
                FROM PointLog pl
                WHERE pl.member.id = :memberId
            """)
    Optional<Integer> sumPointsByMemberId(@Param("memberId") Long memberId);
}
