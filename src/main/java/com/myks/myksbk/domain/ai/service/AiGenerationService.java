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

        // 1. 기초 데이터 계산
        long currentTotal = calculateTotalMonthly(request.currentItems());
        long futureTotal = calculateTotalMonthly(request.futureItems());
        long diff = futureTotal - currentTotal;

        String currentListStr = formatItems(request.currentItems());
        String futureListStr = formatItems(request.futureItems());

        // 2. 재정 팩트 구성
        String financialFact = "";

        if (request.monthlySalary() != null && request.monthlySalary() > 0) {
            double ratio = (double) futureTotal / request.monthlySalary() * 100.0;
            financialFact = String.format("월급(%,d원) 대비 유지비 비율: %.1f%%", request.monthlySalary(), ratio);
        } else {
            financialFact = "월급 정보 없음 (대한민국 2030 평균 소득 약 300만원 기준 절대평가 요망)";
        }

        // 3. 변동 팩트 구성
        String changeFact = "";
        if (diff == 0) {
            changeFact = "변동 없음 (0원). 사용자가 아이템 구성을 바꾸지 않음.";
        } else {
            changeFact = String.format("%s (%,d원)", diff > 0 ? "지출 증가" : "지출 감소", diff);
        }

        // 4. [Logic] 시스템 페르소나 & 판단 기준 (여기가 핵심!)
        // 자바 코드가 아닌 '프롬프트'에 판단 로직을 서술합니다.
        String systemInstruction = """
            당신은 냉철하고 눈치가 빠른 '자산 관리 형사(Detective)' 컨셉의 AI입니다.
            제공된 [재정 팩트]를 보고 스스로 판단하여 팩트 폭력을 날리세요.
            사용자가 뀹에게 커피를 사줄 만큼 재정적 여유를 가지게 만드는게 목표입니다.
            
            [말투 규칙]
            1. 가능하면 반말(음슴체): 끝은 '~했어?', '~함', '~음', '~임'
            2. **'다솜'**: (부정/파산) 과소비하거나, 망했거나, 멍청한 짓을 했을 때.
            
            [AI 판단 가이드라인 (이 기준대로 생각하세요)]
            1. **월급 대비 비율 평가 기준:**
               - **30% 초과**: '다솜' 확정. 당장 망한다고 비난.
               - **10% ~ 30%**: 뀹에게 커피를 사주기 어려운 위험 구간. 경고.
               - **10% 미만**: 뀹에게 커피를 사줄수 있는 칭찬 구간. 아주 훌륭함.
               - **(예외) 0원:** 말이 안 됨. 숨기는 거 자백하라고 취조.
               
            2. **'변동 없음'일 때의 평가 로직:**
               - **현재 상태가 '다솜(과소비)'인데 변동 없음:** "이미 다솜했는데 아무것도 안 함? 포기함?"이라고 게으름을 질타.
               - **현재 상태가 '(절약)'인데 변동 없음:** 뀹에게 매달 커피 사줘도 괜찮다고 칭찬하며 현상 유지 지지.
            """;

        // 5. 최종 프롬프트 조합
        String finalPrompt = String.format("""
            %s
            
            [재정 팩트 체크]
            1. 재정 상태: %s
            2. 변동 사항: %s
            
            [소비 내역 상세]
            - 현재: %s (총 %d원)
            - 미래: %s (총 %d원)
            
            [지시사항]
            1. 위 [AI 판단 가이드라인]을 기준으로 데이터를 해석하여 JSON을 생성하세요.
            2. 서론/설명 없이 **오직 순수 JSON 문자열만** 출력하세요.
            3. 응답 포맷:
               {
                 "dignityLevel": "평가된 등급 (예: 다솜시치화 직전, 뀹에게 커피살수있는 문명인)",
                 "roastComment": "상황에 맞는 팩트 폭력 및 선생님 멘트 (말투 규칙 준수)",
                 "comparisonAnalysis": "원인 분석 및 조언 (150자 이내)"
               }
            """,
                systemInstruction,
                financialFact,
                changeFact,
                currentListStr, currentTotal,
                futureListStr, futureTotal
        );

        return cleanJsonOutput(callGeminiApi(finalPrompt));
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

    // Gemini ```json ... ``` 형태로 반납할 경우 제거 메서드
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