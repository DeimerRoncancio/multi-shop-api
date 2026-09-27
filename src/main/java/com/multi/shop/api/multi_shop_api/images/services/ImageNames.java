package com.multi.shop.api.multi_shop_api.images.services;

import com.multi.shop.api.multi_shop_api.images.entities.Image;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * El orden de las imágenes de un producto vive en su nombre:
 * "camisa-1.webp" va antes que "camisa-2.webp", y la 1 es la principal.
 * Aquí se arma ese nombre y se lee el número para ordenar.
 */
public final class ImageNames {
    private static final Pattern POSITION = Pattern.compile("-(\\d+)(\\.[^.]+)?$");
    private static final Pattern NOT_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");
    private static final Pattern EDGE_DASHES = Pattern.compile("(^-|-$)");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private ImageNames() {}

    public static String slugify(String text) {
        if (text == null || text.isBlank()) return "imagen";

        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        String withoutAccents = DIACRITICS.matcher(normalized).replaceAll("");

        String slug = NOT_ALPHANUMERIC.matcher(withoutAccents.toLowerCase(Locale.ROOT)).replaceAll("-");

        slug = EDGE_DASHES.matcher(slug).replaceAll("");

        return slug.isBlank() ? "imagen" : slug;
    }

    public static int positionOf(String name) {
        if (name == null) return Integer.MAX_VALUE;

        Matcher matcher = POSITION.matcher(name);
        if (!matcher.find()) return Integer.MAX_VALUE;

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException exception) {
            return Integer.MAX_VALUE;
        }
    }

    public static List<Image> sorted(List<Image> images) {
        if (images == null) return List.of();

        return images.stream()
            .sorted(Comparator.comparingInt((Image image) -> positionOf(image.getName())))
            .toList();
    }

    public static Image main(List<Image> images) {
        List<Image> ordered = sorted(images);
        return ordered.isEmpty() ? null : ordered.get(0);
    }

    public static String build(String productName, int position, String originalFilename) {
        return slugify(productName) + "-" + position + extensionOf(originalFilename);
    }

    private static String extensionOf(String filename) {
        if (filename == null) return "";

        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";

        return filename.substring(dot).toLowerCase(Locale.ROOT);
    }
}
