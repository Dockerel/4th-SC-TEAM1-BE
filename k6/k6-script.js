import http from 'k6/http';
import { check, sleep } from 'k6';

// 0~99까지 URL 자동 생성
const BASE = 'http://localhost:8080/api/v1/study/';
const urls1 = Array.from({ length: 100 }, (_, i) => `${BASE}${i}`);
const urls2 = Array.from({ length: 100 }, (_, i) => `${BASE}consume/${i}`);

export const options = {
    vus: 100,          // 동시 사용자 100명
    duration: '10m',   // 10분 지속
};

export default function () {
    // __VU : 1-based VU 번호 → 0-based 인덱스로 변환
    const idx = (__VU - 1) % urls1.length;

    let responses = http.batch([
        {
            method: 'GET',
            url: urls1[idx]
        },
        {
            method: 'GET',
            url: urls2[idx]
        },
    ]);

    check(responses[0], { '일기 작성 성공': (r) => r.status === 200 });
    check(responses[1], { '포인트 소비 성공': (r) => r.status === 200 });

    sleep(1);          // 1초 간격(초당 1요청/VU)
}

