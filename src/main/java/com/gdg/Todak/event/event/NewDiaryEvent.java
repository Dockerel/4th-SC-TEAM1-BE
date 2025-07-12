package com.gdg.Todak.event.event;

import com.gdg.Todak.diary.entity.Diary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NewDiaryEvent {
    private Diary diary;

    public static NewDiaryEvent of(Diary diary) {
        return NewDiaryEvent.builder()
                .diary(diary)
                .build();
    }
}
