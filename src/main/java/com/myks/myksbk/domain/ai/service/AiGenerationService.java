package com.myks.myksbk.domain.ai.service;

import com.myks.myksbk.domain.ai.dto.DignityAiDto;
import com.myks.myksbk.domain.ai.dto.GeminiApiDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerationService {

    @Value("${ai.gemini.api.key}")
    private String apiKey;

    @Value("${ai.gemini.api.url}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();

    public String generateTemplate(com.myks.myksbk.domain.ai.dto.TemplateAiDto.GenerateRequest request) {
        String systemInstruction = getSystemInstruction(request.kind());

        // 프롬프트 강화: 가드레일 및 예시 추가
        String finalPrompt = String.format("""
            %s
            
            [지시사항]
            1. 위 설정된 페르소나에 맞춰 업무용 템플릿을 작성하세요.
            2. 사용자의 입력이 상담 업무와 전혀 관련 없는 잡담, 상식 질문, 코딩 질문, 욕설 등이라면
               템플릿을 생성하지 말고 단호하게 "죄송합니다. 상담 업무와 관련된 주제만 입력해주세요."라고만 출력하세요.
            3. 응답에는 마크다운 코드블록(```)이나 서론/본론 설명을 포함하지 말고, 템플릿 내용만 순수 텍스트로 출력하세요.
            4. 변수가 필요한 곳은 {고객명}, {날짜} 와 같이 중괄호로 표기하세요.
            
            [입력된 주제]: "%s"
            
            [작성된 템플릿]:
            """,
                systemInstruction,
                request.prompt()
        );

        return callGeminiApi(finalPrompt);
    }

    private String getSystemInstruction(String kind) {
        switch (kind) {
            case "case_note":
                return """
                   당신은 전문 CS 상담 이력 관리 시스템입니다.
                   사용자가 입력한 상황을 바탕으로 상담사가 필요한 정보를 이력할 항목으로 구성된 상담 노트를 작성해야 합니다.
                   """;
            case "inquiry_reply":
                return """
                   당신은 고객센터 1:1 문의 답변 담당 AI입니다.
                   사용자가 입력한 상황에 대해 [인사말 -> 공감 및 사과 -> 해결책/답변 -> 추가안내 -> 맺음말] 구조를 갖춘 정중한 답변 메일을 작성해야 합니다.
                   """;
            case "sms_reply":
                return """
                   당신은 고객 안내 문자 발송 시스템입니다.
                   사용자가 입력한 상황을 바탕으로 70자 이내의 간결한 SMS 문구를 작성해야 합니다. 광고성 멘트는 배제하고 핵심 용건만 전달하세요.
                   """;
            default:
                return "상담 업무 보조 AI입니다. 요청에 맞는 비즈니스 템플릿을 작성하세요.";
        }
    }

    /**
     * 품위 유지비 분석 생성 메서드
     */
    public String generateDignityAnalysis(DignityAiDto.GenerateRequest request) {

        // 1. 데이터 가공 (월 유지비 계산 등)
        long currentTotal = calculateTotalMonthly(request.currentItems());
        long futureTotal = calculateTotalMonthly(request.futureItems());
        long diff = futureTotal - currentTotal;

        String currentListStr = formatItems(request.currentItems());
        String futureListStr = formatItems(request.futureItems());

        // 2. 시스템 페르소나 정의 (팩트 폭격기 상담사)
        String systemInstruction = """
            당신은 냉철하고 유머러스하며, 가끔은 비꼬기도 하는 '개인 자산 관리 컨설턴트'입니다.
            사용자의 소비 패턴, 특히 '월 유지비(할부 및 구독)'를 분석하여 뼈 때리는 조언을 해야 합니다.
            말투는 친근한 친구처럼 '반말'을 사용하세요.
            """;

        // 3. 최종 프롬프트 조합
        // 주의: JSON 응답을 강제하기 위해 예시와 제약조건을 명확히 줍니다.
        String finalPrompt = String.format("""
            %s
            
            [분석 대상 데이터]
            1. 현재 상태
               - 아이템: %s
               - 총 월 유지비: %d원
            2. 미래 시뮬레이션 (변경 후)
               - 아이템: %s
               - 총 월 유지비: %d원
               - 월 변동액: %d원 (%s)
            
            [지시사항]
            1. 위 데이터를 비교 분석하여 사용자의 소비 습관을 평가하세요.
            2. 절대 서론이나 설명, 마크다운 코드블록(```json)을 붙이지 말고, **오직 순수 JSON 문자열만** 출력하세요.
            3. 응답 포맷은 아래와 같아야 합니다:
               {
                 "dignityLevel": "평가된 등급 이름 (예: 파산 직전의 얼리어답터, 스마트한 미니멀리스트)",
                 "roastComment": "소비 변화에 대한 냉철하고 위트 있는 팩트 폭력 코멘트 (반말)",
                 "comparisonAnalysis": "비용 증감 원인 분석 및 구체적인 조언 (150자 이내)"
               }
            
            [JSON 출력]:
            """,
                systemInstruction,
                currentListStr, currentTotal,
                futureListStr, futureTotal,
                diff, (diff > 0 ? "증가" : "감소")
        );

        // 4. API 호출 및 결과 정제
        String response = callGeminiApi(finalPrompt);
        return cleanJsonOutput(response);
    }

    // --- 내부 헬퍼 메서드 ---
    private String callGeminiApi(String prompt) {
        try {
            // 1. 공용 DTO 사용 (GeminiApiDto.Request)
            GeminiApiDto.Request requestBody = new GeminiApiDto.Request(prompt);

            URI uri = UriComponentsBuilder.fromUri(URI.create(apiUrl))
                    .queryParam("key", apiKey)
                    .build()
                    .toUri();

            // 2. 공용 DTO로 응답 받기 (GeminiApiDto.Response)
            GeminiApiDto.Response response = restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(GeminiApiDto.Response.class);

            // 3. 헬퍼 메서드로 텍스트 추출
            if (response != null) {
                return response.getText();
            }

            return "AI 응답 없음";

        } catch (Exception e) {
            log.error("Gemini API Error", e);
            return "AI 서비스 오류 발생";
        }
    }

    // Gemini가 가끔 ```json ... ``` 형태로 줄 때가 있어서 이를 제거하는 메서드
    private String cleanJsonOutput(String text) {
        if (text == null) return "{}";
        return text.replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private long calculateTotalMonthly(List<DignityAiDto.ItemDto> items) {
        if (items == null) return 0;
        return items.stream()
                .mapToLong(i -> (i.lifespan() > 0) ? i.price() / i.lifespan() : 0)
                .sum();
    }

    private String formatItems(List<DignityAiDto.ItemDto> items) {
        if (items == null || items.isEmpty()) return "없음";
        return items.stream()
                .map(i -> String.format("%s(%d원/%d개월)", i.name(), i.price(), i.lifespan()))
                .collect(Collectors.joining(", "));
    }
}