package com.staffing.util;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.MessageFormat;

public final class PaginationUtil {
    private static final String HEADER_X_TOTAL_COUNT = "X-Total-Count";
    private static final String HEADER_LINK_FORMAT = "<{0}>; rel=\"{1}\"";

    private PaginationUtil() {
    }

    public static <T> HttpHeaders generatePaginationHeaders(Page<T> page, String baseUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_X_TOTAL_COUNT, Long.toString(page.getTotalElements()));
        
        int pageNumber = page.getNumber();
        int pageSize = page.getSize();
        StringBuilder link = new StringBuilder();

        if (pageNumber < page.getTotalPages() - 1) {
            String nextUrl = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("page", pageNumber + 1)
                    .queryParam("size", pageSize)
                    .toUriString();
            link.append(MessageFormat.format(HEADER_LINK_FORMAT, nextUrl, "next")).append(",");
        }
        if (pageNumber > 0) {
            String prevUrl = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("page", pageNumber - 1)
                    .queryParam("size", pageSize)
                    .toUriString();
            link.append(MessageFormat.format(HEADER_LINK_FORMAT, prevUrl, "prev")).append(",");
        }
        
        String firstUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("page", 0)
                .queryParam("size", pageSize)
                .toUriString();
        link.append(MessageFormat.format(HEADER_LINK_FORMAT, firstUrl, "first")).append(",");
        
        String lastUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("page", page.getTotalPages() - 1)
                .queryParam("size", pageSize)
                .toUriString();
        link.append(MessageFormat.format(HEADER_LINK_FORMAT, lastUrl, "last"));
        
        headers.add(HttpHeaders.LINK, link.toString());
        
        return headers;
    }
} 