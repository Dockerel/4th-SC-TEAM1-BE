package com.gdg.Todak.diary.service;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.diary.dto.*;
import com.gdg.Todak.diary.entity.Diary;
import com.gdg.Todak.diary.repository.DiaryRepository;
import com.gdg.Todak.friend.service.FriendCheckService;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.gdg.Todak.common.exception.errors.DiaryError.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final ImageService imageService;
    private final FriendCheckService friendCheckService;

    @Transactional
    public Diary writeDiary(String userId, DiaryRequest diaryRequest) {
        Member member = getMember(userId);

        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.atTime(23, 59, 59, 99).atZone(ZoneId.systemDefault()).toInstant();

        if (diaryRepository.existsByMemberAndCreatedAtBetween(member, startOfDay, endOfDay)) {
            throw new TodakException(CONFLICT_DIARY_ERROR);
        }

        Diary diary = Diary.builder()
                .member(member)
                .content(diaryRequest.content())
                .emotion(diaryRequest.emotion())
                .storageUUID(diaryRequest.storageUUID())
                .build();

        return diaryRepository.save(diary);
    }

    public List<DiarySummaryResponse> getMySummaryByYearAndMonth(String userId, DiarySearchRequest diarySearchRequest) {
        Member member = getMember(userId);

        int year = diarySearchRequest.year();
        int month = diarySearchRequest.month();

        if (month < 1 || month > 12) {
            throw new TodakException(MONTH_RANGE_ERROR);
        }

        List<Diary> diaries = getDiariesByYearAndMonth(year, month, member);

        if (!diaries.isEmpty() && !diaries.getFirst().isWriter(member)) {
            throw new TodakException(NOT_DIARY_OWNER_ERROR);
        }

        return diaries.stream()
                .map(diary -> new DiarySummaryResponse(
                        diary.getId(),
                        diary.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        diary.getEmotion()
                )).toList();
    }

    public List<DiarySummaryResponse> getFriendSummaryByYearAndMonth(String userId, String friendId, DiarySearchRequest diarySearchRequest) {
        Member friendMember = getMember(friendId);

        int year = diarySearchRequest.year();
        int month = diarySearchRequest.month();

        if (month < 1 || month > 12) {
            throw new TodakException(MONTH_RANGE_ERROR);
        }

        List<Member> acceptedMembers = friendCheckService.getFriendMembers(userId);

        if (!acceptedMembers.contains(friendMember)) {
            throw new TodakException(NOT_FRIEND_ERROR);
        }

        List<Diary> diaries = getDiariesByYearAndMonth(year, month, friendMember);

        return diaries.stream()
                .map(diary -> new DiarySummaryResponse(
                        diary.getId(),
                        diary.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        diary.getEmotion()
                )).toList();
    }

    private List<Diary> getDiariesByYearAndMonth(int year, int month, Member member) {
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(99);

        Instant startInstant = startOfMonth.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endOfMonth.atZone(ZoneId.systemDefault()).toInstant();

        return diaryRepository.findByMemberAndCreatedAtBetween(member, startInstant, endInstant);
    }

    public DiaryDetailResponse readDiary(String userId, Long diaryId) {
        Member member = getMember(userId);

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new TodakException(DIARY_NOT_FOUND_BY_DIARY_ID_ERROR));

        LocalDateTime createdAt = diary.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (diary.isWriter(member)) {
            return new DiaryDetailResponse(diary.getId(), createdAt, diary.getContent(), diary.getEmotion(), diary.getStorageUUID(), true);
        }

        List<Member> acceptedMembers = friendCheckService.getFriendMembers(diary.getMember().getUserId());

        if (!acceptedMembers.contains(member)) {
            throw new TodakException(NOT_ALLOWED_DIARY_LOOK_UP_MEMBER_ERROR);
        }

        return new DiaryDetailResponse(diary.getId(), createdAt, diary.getContent(), diary.getEmotion(), diary.getStorageUUID(), false);
    }

    @Transactional
    public void updateDiary(String userId, Long diaryId, DiaryUpdateRequest diaryUpdateRequest) {
        Member member = getMember(userId);

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new TodakException(DIARY_NOT_FOUND_BY_DIARY_ID_ERROR));

        if (!diary.isWriter(member)) {
            throw new TodakException(NOT_DIARY_OWNER_ERROR);
        }

        diary.updateDiary(diaryUpdateRequest.content(), diaryUpdateRequest.emotion());
    }

    @Transactional
    public void deleteDiary(String userId, Long diaryId) {
        Member member = getMember(userId);

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new TodakException(DIARY_NOT_FOUND_BY_DIARY_ID_ERROR));

        if (!diary.isWriter(member)) {
            throw new TodakException(NOT_DIARY_OWNER_ERROR);
        }

        imageService.deleteAllImagesInStorageUUID(userId, diary.getStorageUUID());
        diaryRepository.delete(diary);
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new TodakException(USER_NOT_FOUND_BY_USER_ID_ERROR));
    }
}
