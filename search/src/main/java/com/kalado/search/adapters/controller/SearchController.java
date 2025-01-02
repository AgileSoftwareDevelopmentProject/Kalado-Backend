package com.kalado.search.adapters.controller;

import com.kalado.search.application.service.SearchService;
import com.kalado.search.domain.model.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/products")
    public ResponseEntity<Page<ProductDocument>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) Integer fromYear,
            @RequestParam(required = false) Integer toYear,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer distance,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        SortOrder order = sortOrder != null ? SortOrder.valueOf(sortOrder.toUpperCase()) : SortOrder.DESC;
        Page<ProductDocument> results = searchService.searchProducts(
                keyword, minPrice, maxPrice, fromDate, toDate,
                fromYear, toYear, category, brand, location, distance,
                sortBy, order, PageRequest.of(page, size)
        );
        return ResponseEntity.ok(results);
    }
}