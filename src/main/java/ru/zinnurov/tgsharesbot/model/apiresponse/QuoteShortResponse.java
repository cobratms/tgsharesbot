package ru.zinnurov.tgsharesbot.model.apiresponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ответ на запрос сокращенной котировки акции, включая цену, изменения и объем.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QuoteShortResponse {

    /**
     * Имя акции.
     */
    private String symbol;

    /**
     * Цена в момент ответа.
     */
    private String price;

    /**
     * Объем торгов в момент ответа.
     */
    private String volume;
}
