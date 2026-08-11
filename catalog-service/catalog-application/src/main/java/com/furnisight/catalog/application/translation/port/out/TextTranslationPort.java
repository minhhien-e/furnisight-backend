package com.furnisight.catalog.application.translation.port.out;

public interface TextTranslationPort {
    String translate(String text, String sourceLang, String targetLang);
    java.util.List<String> translateBatch(java.util.List<String> texts, String sourceLang, String targetLang);
}
