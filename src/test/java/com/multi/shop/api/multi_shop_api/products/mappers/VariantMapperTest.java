package com.multi.shop.api.multi_shop_api.products.mappers;

import com.multi.shop.api.multi_shop_api.products.dtos.VariantDTO;
import com.multi.shop.api.multi_shop_api.products.entities.Variant;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VariantMapperTest {
    @Test
    void splitsTheValuesStoredInASingleColumn() {
        assertThat(VariantMapper.MAPPER.splitValues("S|M|L")).containsExactly("S", "M", "L");
        assertThat(VariantMapper.MAPPER.splitValues("Unica")).containsExactly("Unica");
    }

    @Test
    void returnsNoValuesWhenTheColumnIsEmptyOrNull() {
        assertThat(VariantMapper.MAPPER.splitValues(null)).isEmpty();
        assertThat(VariantMapper.MAPPER.splitValues("")).isEmpty();
        assertThat(VariantMapper.MAPPER.splitValues("   ")).isEmpty();
    }

    @Test
    void joinsTheValuesBackIntoOneColumn() {
        assertThat(VariantMapper.MAPPER.joinValues(List.of("S", "M", "L"))).isEqualTo("S|M|L");
        assertThat(VariantMapper.MAPPER.joinValues(List.of())).isEmpty();
        assertThat(VariantMapper.MAPPER.joinValues(null)).isEmpty();
    }

    @Test
    void mapsAVariantWithItsSeparatedValues() {
        VariantDTO dto = VariantMapper.MAPPER.toVariantDTO(variant("variant-id", "Talla", "size", "talla", "S|M|L"));

        assertThat(dto.id()).isEqualTo("variant-id");
        assertThat(dto.name()).isEqualTo("Talla");
        assertThat(dto.type()).isEqualTo("size");
        assertThat(dto.tag()).isEqualTo("talla");
        assertThat(dto.listValues()).containsExactly("S", "M", "L");
    }

    @Test
    void mapsEveryVariantOfAProduct() {
        List<VariantDTO> dtos = VariantMapper.MAPPER.toVariantDTOs(List.of(
            variant("1", "Talla", "size", "talla", "S|M"),
            variant("2", "Color", "color", "color", "Rojo")
        ));

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).listValues()).containsExactly("S", "M");
        assertThat(dtos.get(1).listValues()).containsExactly("Rojo");
    }

    @Test
    void mapsAVariantWithoutValues() {
        VariantDTO dto = VariantMapper.MAPPER.toVariantDTO(variant("id", "Talla", "size", "talla", null));

        assertThat(dto.listValues()).isEmpty();
    }

    private static Variant variant(String id, String name, String type, String tag, String values) {
        Variant variant = new Variant();
        variant.setId(id);
        variant.setName(name);
        variant.setType(type);
        variant.setTag(tag);
        variant.setValues(values);
        return variant;
    }
}
