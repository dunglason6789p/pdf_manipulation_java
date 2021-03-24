package model;

import java.util.Optional;

public class WordInfo {
    public final String kanji;
    public final String hiragana;
    public final String nom;
    public final String meaning;

    public WordInfo(String kanji, String hiragana, String nom, String meaning) {
        this.kanji = Optional.ofNullable(kanji).orElse("");
        this.hiragana = Optional.ofNullable(hiragana).orElse("");
        this.nom = Optional.ofNullable(nom).orElse("");
        this.meaning = Optional.ofNullable(meaning).orElse("");
    }
}
