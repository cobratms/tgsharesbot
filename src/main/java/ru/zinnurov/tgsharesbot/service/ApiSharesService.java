package ru.zinnurov.tgsharesbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.zinnurov.tgsharesbot.config.RestTemplateConfig;
import ru.zinnurov.tgsharesbot.model.apiresponse.QuoteShortResponse;
import ru.zinnurov.tgsharesbot.utils.ApiCommandsDictionary;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Сервис для работы с API по получению данных об акциях.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiSharesService {

    private final RestTemplateConfig config;
    private final RestTemplate restTemplate;

    /**
     * Получение цены акции в момент обращения к api.
     *
     * @param shareName наименование акции.
     * @return цена акции
     */
    public String getSharePriceByName(String shareName) {
        String url = this.config.getCommonUrl()
                + ApiCommandsDictionary.QUOTE_SHORT
                + "/"
                + shareName;
        String price = "";
        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("apikey", this.config.getApiKey())
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        ResponseEntity<QuoteShortResponse[]> response = this.restTemplate.getForEntity(uri, QuoteShortResponse[].class);
        if (response.getStatusCode().is2xxSuccessful()
                && response.getBody() != null
                && Arrays.stream(response.getBody()).findFirst().isPresent()) {
            price = Arrays.stream(response.getBody()).findFirst().get().getPrice();
        }
        return price;
    }

}
