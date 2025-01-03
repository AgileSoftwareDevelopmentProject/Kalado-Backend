package com.kalado.search.domain.model.mapper;

import com.kalado.common.dto.ProductDto;
import com.kalado.search.domain.model.ProductDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductSearchMapper {

    @Mapping(target = "id", expression = "java(String.valueOf(productDto.getId()))")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "timestampToLocalDateTime")
    ProductDocument dtoToDocument(ProductDto productDto);

    @Mapping(target = "id", expression = "java(Long.valueOf(document.getId()))")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "localDateTimeToTimestamp")
    ProductDto documentToDto(ProductDocument document);

    default Page<ProductDto> toProductDtoPage(Page<ProductDocument> page) {
        return page.map(this::documentToDto);
    }

    default List<ProductDto> toDtoList(List<ProductDocument> documents) {
        return documents.stream()
                .map(this::documentToDto)
                .collect(Collectors.toList());
    }

    @Named("timestampToLocalDateTime")
    default LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    @Named("localDateTimeToTimestamp")
    default Timestamp localDateTimeToTimestamp(LocalDateTime localDateTime) {
        return localDateTime != null ? Timestamp.valueOf(localDateTime) : null;
    }
}