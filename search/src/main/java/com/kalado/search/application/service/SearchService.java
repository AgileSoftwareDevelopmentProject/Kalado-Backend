package com.kalado.search.application.service;

import com.kalado.search.domain.model.ProductDocument;
import com.kalado.search.infrastructure.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public Page<ProductDocument> searchProducts(
            String keyword,
            Double minPrice,
            Double maxPrice,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Integer fromYear,
            Integer toYear,
            String category,
            String brand,
            String location,
            Integer distance,
            String sortBy,
            SortOrder sortOrder,
            Pageable pageable
    ) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // Keyword search across multiple fields
        if (keyword != null && !keyword.trim().isEmpty()) {
            boolQuery.must(QueryBuilders.multiMatchQuery(keyword)
                    .field("title", 2.0f)
                    .field("title.english")
                    .field("description")
                    .field("description.english")
                    .field("brand")
                    .type("best_fields")
                    .fuzziness("AUTO"));
        }

        // Price range
        if (minPrice != null || maxPrice != null) {
            boolQuery.must(QueryBuilders.rangeQuery("price.amount")
                    .from(minPrice)
                    .to(maxPrice));
        }

        // Date range
        if (fromDate != null || toDate != null) {
            boolQuery.must(QueryBuilders.rangeQuery("createdAt")
                    .from(fromDate)
                    .to(toDate));
        }

        // Production year range
        if (fromYear != null || toYear != null) {
            boolQuery.must(QueryBuilders.rangeQuery("productionYear")
                    .from(fromYear)
                    .to(toYear));
        }

        // Category filter
        if (category != null && !category.trim().isEmpty()) {
            boolQuery.must(QueryBuilders.termQuery("category", category));
        }

        // Brand filter
        if (brand != null && !brand.trim().isEmpty()) {
            boolQuery.must(QueryBuilders.termQuery("brand", brand));
        }

        // Location-based search
        if (location != null && !location.trim().isEmpty() && distance != null) {
            boolQuery.must(QueryBuilders.geoDistanceQuery("location")
                    .point(location)
                    .distance(distance, DistanceUnit.KILOMETERS));
        }

        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable);

        // Sorting
        if (sortBy != null && sortOrder != null) {
            switch (sortBy) {
                case "price" -> searchQueryBuilder.withSort(SortBuilders.fieldSort("price.amount").order(sortOrder));
                case "date" -> searchQueryBuilder.withSort(SortBuilders.fieldSort("createdAt").order(sortOrder));
                case "year" -> searchQueryBuilder.withSort(SortBuilders.fieldSort("productionYear").order(sortOrder));
            }
        } else {
            // Default sort by creation date, newest first
            searchQueryBuilder.withSort(SortBuilders.fieldSort("createdAt").order(SortOrder.DESC));
        }

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(
                searchQueryBuilder.build(),
                ProductDocument.class
        );

        List<ProductDocument> products = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return Page.empty(); // TODO: Implement proper pagination
    }

    public void indexProduct(ProductDocument product) {
        productSearchRepository.save(product);
    }

    public void deleteProduct(String id) {
        productSearchRepository.deleteById(id);
    }
}