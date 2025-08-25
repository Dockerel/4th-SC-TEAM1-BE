package com.gdg.Todak.study.controller;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.diary.Emotion;
import com.gdg.Todak.diary.dto.DiaryRequest;
import com.gdg.Todak.diary.facade.DiaryFacade;
import com.gdg.Todak.diary.repository.DiaryRepository;
import com.gdg.Todak.point.scheduler.PointReconciliationScheduler;
import com.gdg.Todak.study.service.StudyService;
import com.gdg.Todak.tree.business.TreeService;
import com.gdg.Todak.tree.domain.GrowthButton;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/study")
public class StudyController {

    private final StudyService studyService;
    private final DiaryRepository diaryRepository;
    private final DiaryFacade diaryFacade;
    private final TreeService treeService;
    private final PointReconciliationScheduler pointReconciliationScheduler;

    //    @GetMapping("/{num}")
    public ApiResponse<String> saveDiary(@PathVariable("num") String num) {
        Long id = studyService.saveDiary(num);
        diaryRepository.deleteById(id);
        return ApiResponse.ok("실행완료");
    }

    @GetMapping("/{num}")
    public ApiResponse<String> saveDiaryV2(@PathVariable("num") String num) {
        Long id = diaryFacade.writeDiary("userId" + num, new DiaryRequest("content", Emotion.HAPPY, "storageUUID"));
        diaryRepository.deleteById(id);
        return ApiResponse.ok("실행완료");
    }

    @GetMapping("/consume/{num}")
    public ApiResponse<String> consumePoint(@PathVariable("num") String num) {
        String userId = "userId" + String.valueOf(num);
        String response = treeService.earnExperience(userId, GrowthButton.NUTRIENT);
        return ApiResponse.ok(response + " : " + userId);
    }

    @GetMapping("/transactional")
    public ApiResponse<String> transactionalEndpoint() {
        studyService.transactionalTest();
        return ApiResponse.ok("success");
    }

    @GetMapping("/point-recon")
    public ApiResponse<String> pointRecon() {
        pointReconciliationScheduler.reconcilePoints();
        return ApiResponse.ok("success");
    }

}
