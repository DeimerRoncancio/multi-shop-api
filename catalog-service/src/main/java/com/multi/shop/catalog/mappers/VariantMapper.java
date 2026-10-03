package com.multi.shop.catalog.mappers;

import com.multi.shop.catalog.dtos.VariantDTO;
import com.multi.shop.catalog.dtos.VariantResponseDTO;
import com.multi.shop.catalog.entities.Variant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VariantMapper {
    List<VariantDTO> toVariantDTOs(List<Variant> variants);

    @Mapping(target = "listValues", source = "values", qualifiedByName = "splitValues")
    VariantDTO toVariantDTO(Variant variant);

    @Mapping(target = "listValues", expression = "java(values)")
    VariantResponseDTO variantToDTO(Variant variant, List<String> values);

    @Mapping(target = "values", expression = "java(listValues)")
    Variant dtoToVariant(VariantDTO variantDTO, String listValues);

    @Named("splitValues")
    default List<String> splitValues(String values) {
        return values == null || values.isBlank()
            ? List.of()
            : List.of(values.split("\\|"));
    }

    default String joinValues(List<String> values) {
        return values == null ? "" : String.join("|", values);
    }
}
