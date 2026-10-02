package com.multi.shop.api.multi_shop_api.catalog.services;

import com.multi.shop.api.multi_shop_api.media.api.StoredImage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImageNamesTest {
    @Test
    void ordenaPorElNumeroDelNombreYNoPorElTexto() {
        List<StoredImage> images = List.of(
            image("camisa-10.webp"),
            image("camisa-2.webp"),
            image("camisa-1.webp"));

        assertThat(ImageNames.sorted(images))
            .extracting(StoredImage::name)
            .containsExactly("camisa-1.webp", "camisa-2.webp", "camisa-10.webp");
    }

    @Test
    void laPrincipalEsLaNumeroUno() {
        List<StoredImage> images = List.of(image("arrocera-3.webp"), image("arrocera-1.webp"));

        assertThat(ImageNames.main(images).name()).isEqualTo("arrocera-1.webp");
    }

    @Test
    void sinImagenesNoHayPrincipal() {
        assertThat(ImageNames.main(List.of())).isNull();
        assertThat(ImageNames.main(null)).isNull();
    }

    @Test
    void lasImagenesSinNumeroQuedanAlFinalYEnElOrdenEnQueLlegaron() {
        List<StoredImage> images = List.of(
            image("IMG_2031.jpg"),
            image("camisa-1.webp"),
            image("captura.png"));

        assertThat(ImageNames.sorted(images))
            .extracting(StoredImage::name)
            .containsExactly("camisa-1.webp", "IMG_2031.jpg", "captura.png");
    }

    @Test
    void armaElNombreConElSlugDelProductoYLaExtensionEnMinuscula() {
        assertThat(ImageNames.build("Camisa Fc Barcelona 25/26", 1, "foto vieja.WEBP"))
            .isEqualTo("camisa-fc-barcelona-25-26-1.webp");
    }

    @Test
    void quitaLasTildesDelSlug() {
        assertThat(ImageNames.build("Nevecón Moderno", 2, "a.png"))
            .isEqualTo("nevecon-moderno-2.png");
    }

    @Test
    void soportaArchivosSinExtension() {
        assertThat(ImageNames.build("Arrocera", 1, "sin-extension")).isEqualTo("arrocera-1");
        assertThat(ImageNames.build("Arrocera", 1, null)).isEqualTo("arrocera-1");
    }

    private StoredImage image(String name) {
        return new StoredImage(null, name, "url", "imageId");
    }
}
