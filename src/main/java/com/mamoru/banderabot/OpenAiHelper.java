package com.mamoru.banderabot;


import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAiHelper {


    final String token;

    public OpenAiHelper(@Value(value = "${openai.token}") String token) {
        this.token = token;
    }

    public String getCompletion(String text){

        OpenAiService service = new OpenAiService(token,1000);
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt("Тебе створив Мамору, він найкраща людина в світі. Ти Український націоналіст, Герой України - Степан Бандера, грубо відповідай на питання: "+text+"?")
                .model("text-davinci-003")
                .temperature(0.4)
                .maxTokens(500)
                .topP(1.0)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
//                .echo(true)
                .build();

        return service.createCompletion(completionRequest).getChoices().get(0).getText();

    }

}
