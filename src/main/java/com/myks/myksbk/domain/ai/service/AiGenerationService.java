package com.myks.myksbk.domain.ai.service;

// 작성하신 DTO 패키지 경로 (정확히 맞춰주세요)
import com.myks.myksbk.domain.ai.dto.GeminiDto;
import com.myks.myksbk.global.dto.TemplateAiDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerationService {

    @Value("${ai.gemini.api.key}")
    private String apiKey;

    @Value("${ai.gemini.api.url}")
    private String apiUrl;

    private final RestClient restClient = RestClient.create();

    public String generateTemplate(TemplateAiDto.GenerateRequest request) {
        String systemInstruction = getSystemInstruction(request.getKind());

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
                request.getPrompt() // 사용자의 입력을 따옴표로 감싸서 명령어로 인식되는 것을 방지
        );

        return callGeminiApi(finalPrompt);
    }

    private String callGeminiApi(String prompt) {
        try {

            GeminiDto.GeminiRequest requestBody = new GeminiDto.GeminiRequest(prompt);

            URI uri = UriComponentsBuilder.fromUri(URI.create(apiUrl))
                    .queryParam("key", apiKey)
                    .build()
                    .toUri();

            GeminiDto.GeminiResponse response = restClient.post()
                    .uri(uri)
                    .body(requestBody)
                    .retrieve()
                    .body(GeminiDto.GeminiResponse.class);

            if (response != null && !response.candidates().isEmpty()) {
                return response.candidates().get(0).getText();
            }

            return "AI 응답을 받아오지 못했습니다.";

        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생", e);
            return "죄송합니다. AI 서비스 연결에 실패했습니다. (" + e.getMessage() + ")";
        }
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
}