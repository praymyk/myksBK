package com.myks.myksbk.domain.ai.service;

import com.myks.myksbk.global.dto.TemplateAiDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiGenerationService {

    // application.yml에 설정된 키 (예시)
    // @Value("${ai.gemini.api-key}")
    // private String apiKey;

    public String generateTemplate(TemplateAiDto.GenerateRequest request) {
        // 1. 템플릿 종류에 따른 '페르소나(역할)' 및 '포맷' 정의
        String systemInstruction = getSystemInstruction(request.getKind());

        // 2. 최종 프롬프트 조합
        String finalPrompt = String.format(
                "%s\n\n[사용자 요구사항]: %s\n\n[조건]: 변수가 들어갈 곳은 {고객명}, {날짜} 처럼 중괄호로 표시해줘.",
                systemInstruction,
                request.getPrompt()
        );

        // 3. 실제 LLM (Gemini/GPT) API 호출
        // return callLlmApi(finalPrompt);

        // [임시] 실제 연동 전 테스트용 응답 (나중에 여기를 API 호출로 교체하세요)
        return mockLlmCall(finalPrompt);
    }

    private String getSystemInstruction(String kind) {
        switch (kind) {
            case "case_note":
                return "당신은 전문 CS 상담사입니다. 상담 이력을 기록하기 위한 깔끔하고 구조화된 '상담 노트 템플릿'을 작성하세요. 요약, 상세내용, 조치사항, 추후계획 등으로 섹션을 나누어 마크다운 형식으로 작성하세요.";
            case "inquiry_reply":
                return "당신은 친절한 고객지원 담당자입니다. 1:1 문의에 대한 정중하고 명확한 '답변 템플릿'을 작성하세요. 서론(인사/공감), 본론(해결책), 결론(추가안내/마무리) 구조를 갖추세요.";
            case "sms_reply":
                return "당신은 고객에게 문자를 보내는 담당자입니다. 70자 이내로 핵심만 전달하는 'SMS 답변 템플릿'을 작성하세요. 광고성 멘트 없이 용건만 정중하게 작성하세요.";
            default:
                return "요청에 맞는 적절한 텍스트 템플릿을 작성하세요.";
        }
    }

    // [TODO] 여기에 WebClient나 Spring AI를 사용하여 실제 Gemini/GPT 호출 로직 구현
    private String mockLlmCall(String prompt) {
        // 실제 연동 전에는 그냥 받은 텍스트를 가공해서 리턴
        try { Thread.sleep(1500); } catch (InterruptedException e) {} // 로딩 흉내
        return "🤖 [AI 생성 결과]\n\n" + prompt + "\n\n(실제 AI 연동이 필요합니다.)";
    }
}