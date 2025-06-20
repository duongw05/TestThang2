package com.example.baitapcuoiki2.i18n;

import com.example.baitapcuoiki2.exception.CustomException.InvalidLocaleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CustomLocaleResolver implements LocaleResolver {

    private final List<Locale> supportedLocales = Arrays.asList(
            Locale.ENGLISH,
            new Locale("vi")
    );

    private final Locale defaultLocale = new Locale("vi");

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String headerLang = request.getHeader("Accept-Language");
        if (headerLang == null || headerLang.isEmpty()) {
            return defaultLocale;
        }

        try {
            List<Locale.LanguageRange> list = Locale.LanguageRange.parse(headerLang);
            Locale locale = Locale.lookup(list, supportedLocales);
            return (locale != null) ? locale : defaultLocale;
        } catch (IllegalArgumentException ex) {
            throw new InvalidLocaleException("Ngôn ngữ không hợp lệ: " + headerLang);
        }
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {

    }
}
