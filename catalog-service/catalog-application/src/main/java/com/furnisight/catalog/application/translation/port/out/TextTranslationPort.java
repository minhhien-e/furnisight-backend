package com.furnisight.catalog.application.translation.port.out;

public interface TextTranslationPort {
    String translate(String text, String sourceLang, String targetLang);
}
