package com.multi.shop.api.multi_shop_api.products.mappers;

import com.multi.shop.api.multi_shop_api.products.dtos.VariantDTO;
import com.multi.shop.api.multi_shop_api.products.dtos.VariantResponseDTO;
import com.multi.shop.api.multi_shop_api.products.entities.Variant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface VariantMapper {
    VariantMapper MAPPER = Mappers.getMapper(VariantMapper.class);

    // Nivel 1: lo usa ProductServiceImpl para las respuestas con productos
    List<VariantDTO> toVariantDTOs(List<Variant> variants);

    // Nivel 2: lo usa toVariantDTOs
    @Mapping(target = "listValues", source = "values", qualifiedByName = "splitValues")
    VariantDTO toVariantDTO(Variant variant);

    // Nivel 1: los usa VariantServiceImpl
    @Mapping(target = "listValues", expression = "java(values)")
    VariantResponseDTO variantToDTO(Variant variant, List<String> values);

    @Mapping(target = "values", expression = "java(listValues)")
    Variant dtoToVariant(VariantDTO variantDTO, String listValues);

    // Nivel 2: separa y une los valores, que en la base van en una sola columna
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
