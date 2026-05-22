package com.example.engine

import java.util.Locale

object LocalClassifier {

    private val QURAN_KEYWORDS = listOf(
        "ياسر الدوسري", "عبد الباسط", "ماهر المعيقلي", "العفاسي", "المنشاوي", "السديس", "الشريم", "الحصري", "المعيقلي",
        "الدوسري", "قرآن", "قران", "سورة", "سوره", "تلاوة", "تلاوه", "المصحف", "تجويد", "ترتيل", "رواية", "ورش", "حفص", 
        "quran", "surah", "surat", "recitation", "reciter", "recited", "koran", "mushaf", "tilawat", "yasser", "dosari", 
        "abdulbasit", "basit", "maher", "muaiqly", "al afasy", "afasy", "sudais", "minshawi", "shuraim", "hussary"
    )

    private val MUSIC_KEYWORDS = listOf(
        "أغنية", "اغنية", "اغاني", "مهرجان", "موزيك", "ألبوم", "لحن", "شاكوش", "عمرو دياب", "حماقي", "تامر حسني", 
        "شيرين", "طرب", "رامي", "انغام", "نانسي", "اليسا", "فيروز", "song", "songs", "music", "remix", "beat", "instrumental", 
        "pop", "rock", "rap", "metal", "dj", "track", "album", "singer", "concert"
    )

    private val LECTURE_KEYWORDS = listOf(
        "خطبة", "خطبه", "محاضرة", "محاضره", "شيخ", "تفسير", "درس", "موعظة", "موعظه", "فتاوى", "شرح", "البلقاسي",
        "النابلسي", "العريفي", "حسان", "يعقوب", "الحويني", "السيرة", "سيرة", "سلسلة", "lecture", "speech", "khutbah",
        "fatwa", "sermon", "sheikh", "shani", "dr", "islamic talk"
    )

    private val SURAHS_ARABIC = listOf(
        "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس", "هود",
        "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه", "الأنبياء", "الحج", "المؤمنون",
        "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم", "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر",
        "يس", "الصافات", "ص", "الزمر", "غافر", "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد",
        "الفتح", "الحجرات", "ق", "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة",
        "الحشر", "الممتحنة", "الصف", "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة",
        "المعارج", "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس",
        "التكوير", "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد", "الشمس",
        "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات", "القارعة", "التكاثر",
        "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر", "المسد", "الإخلاص", "الفلق", "الناس"
    )

    private val SURAHS_ENGLISH = listOf(
        "Al-Fatihah", "Al-Baqarah", "Ali 'Imran", "An-Nisa'", "Al-Ma'idah", "Al-An'am", "Al-A'raf", "Al-Anfal", "At-Tawbah",
        "Yunus", "Hud", "Yusuf", "Ar-Ra'd", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra'", "Al-Kehf", "Maryam", "Taha",
        "Al-Anbiya'", "Al-Hajj", "Al-Mu'minun", "An-Nur", "Al-Furqan", "Ash-Shu'ara'", "An-Naml", "Al-Qasas", "Al-'Ankabut",
        "Ar-Rum", "Luqman", "As-Sajdah", "Al-Ahzab", "Saba'", "Fatir", "Yaseen", "As-Saffat", "Sad", "Az-Zumar", "Ghafir",
        "Fussilat", "Ash-Shura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jathiyah", "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat",
        "Qaf", "Adh-Dhariyat", "At-Tur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqi'ah", "Al-Hadid", "Al-Mujadilah",
        "Al-Hashr", "Al-Mumtahanah", "As-Saff", "Al-Jumu'ah", "Al-Munafiqun", "At-Taghabun", "At-Talaq", "At-Tahrim",
        "Al-Mulk", "Al-Qalam", "Al-Haqqah", "Al-Ma'arij", "Nuh", "Al-Jinn", "Al-Muzzammil", "Al-Muddaththir", "Al-Qiyamah",
        "Al-Insan", "Al-Mursalat", "An-Naba'", "An-Naziat", "Abasa", "At-Takwir", "Al-Infitar", "Al-Mutaffifin", "Al-Inshiqaq",
        "Al-Buruj", "Al-Tariq", "Al-A'la", "Al-Ghashiyah", "Al-Fajr", "Al-Balad", "Al-Shams", "Al-Layl", "Al-Duha",
        "Al-Inshirah", "Al-Tin", "Al-Alaq", "Al-Qadr", "Al-Bayyinah", "Al-Zalzalah", "Al-Adiyat", "Al-Qari'ah", "At-Takathur",
        "Al-Asr", "Al-Humazah", "Al-Fil", "Quraysh", "Al-Ma'un", "Al-Kawthar", "Al-Kafirun", "An-Nasr", "Al-Masad",
        "Al-Ikhlas", "Al-Falaq", "An-Nas"
    )

    private val QARIS_DICT = listOf(
        "ياسر الدوسري" to "Yasser Al-Dosari",
        "عبد الباسط عبد الصمد" to "Abdul Basit",
        "ماهر المعيقلي" to "Maher Al-Muaiqly",
        "مشاري العفاسي" to "Mishary Al-Afasy",
        "محمود خليل الحصري" to "Mahmoud Khalil Al-Hussary",
        "محمد صديق المنشاوي" to "Mohamed Siddiq Al-Minshawi",
        "عبد الرحمن السديس" to "Abdul Rahman Al-Sudais",
        "سعود الشريم" to "Saud Al-Shuraim",
        "سعد الغامدي" to "Saad Al-Ghamdi",
        "أبو بكر الشاطري" to "Abu Bakr Al-Shatri"
    )

    data class ClassificationResult(
        val category: String, // QURAN, MUSIC, LECTURE, OTHER
        val confidence: Float,
        val detectedQari: String?,
        val detectedSurah: String?
    )

    fun classify(title: String, artist: String?, album: String?, filePath: String): ClassificationResult {
        val titleNorm = title.lowercase()
        val artistNorm = (artist ?: "").lowercase()
        val albumNorm = (album ?: "").lowercase()
        val pathNorm = filePath.lowercase()

        val fullText = "$titleNorm $artistNorm $albumNorm $pathNorm"

        // Calculate scores for each category
        var quranScore = 0f
        var musicScore = 0f
        var lectureScore = 0f

        // Keyword checking
        for (kw in QURAN_KEYWORDS) {
            if (fullText.contains(kw.lowercase())) {
                quranScore += 1.5f
            }
        }
        for (kw in MUSIC_KEYWORDS) {
            if (fullText.contains(kw.lowercase())) {
                musicScore += 1.5f
            }
        }
        for (kw in LECTURE_KEYWORDS) {
            if (fullText.contains(kw.lowercase())) {
                lectureScore += 1.5f
            }
        }

        // Surah detection
        var detectedSurah: String? = null
        for (idx in SURAHS_ARABIC.indices) {
            val sAr = SURAHS_ARABIC[idx]
            val sEn = SURAHS_ENGLISH[idx].lowercase()
            val sEnClean = sEn.replace("-", "").replace("'", "")

            if (titleNorm.contains(sAr) || pathNorm.contains(sAr) || albumNorm.contains(sAr)) {
                quranScore += 3.0f
                detectedSurah = SURAHS_ENGLISH[idx]
                break
            } else if (titleNorm.contains(sEn) || pathNorm.contains(sEn) ||
                       titleNorm.contains(sEnClean) || pathNorm.contains(sEnClean)) {
                quranScore += 3.0f
                detectedSurah = SURAHS_ENGLISH[idx]
                break
            }
        }

        // Qari detection
        var detectedQari: String? = null
        for (qariPair in QARIS_DICT) {
            val qAr = qariPair.first
            val qArParts = qAr.split(" ")
            val qEn = qariPair.second.lowercase()
            val qEnParts = qEn.split(" ")

            val matchesAr = qArParts.any { it.length > 3 && fullText.contains(it) }
            val matchesEn = qEnParts.any { it.length > 3 && fullText.contains(it) }

            if (matchesAr || matchesEn) {
                quranScore += 3.0f
                detectedQari = qariPair.second
                break
            }
        }

        // Additional heuristics (e.g. standard file directories)
        if (filePath.contains("quran") || filePath.contains("قرآن")) {
            quranScore += 2.0f
        }
        if (filePath.contains("music") || filePath.contains("music_app") || filePath.contains("song")) {
            musicScore += 2.0f
        }

        // Decision logic
        val maxScore = maxOf(quranScore, musicScore, lectureScore)
        if (maxScore == 0f) {
            return ClassificationResult("OTHER", 1.0f, null, null)
        }

        val totalScore = quranScore + musicScore + lectureScore
        val confidence = maxScore / totalScore

        return when (maxScore) {
            quranScore -> ClassificationResult("QURAN", confidence, detectedQari ?: artist ?: "Mishary Al-Afasy", detectedSurah)
            musicScore -> ClassificationResult("MUSIC", confidence, artist ?: "Unknown Artist", null)
            lectureScore -> ClassificationResult("LECTURE", confidence, artist ?: "Unknown Speaker", null)
            else -> ClassificationResult("OTHER", confidence, null, null)
        }
    }

    /**
     * Smart spelling search for Arab Names (Levenstein distance distance fuzzy search).
     * Helps user type "دوسري" and find "Yasser Al-Dosari" correctly.
     */
    fun matchesFuzzy(query: String, target: String): Boolean {
        val q = query.trim().lowercase()
        val t = target.lowercase()
        if (t.contains(q)) return true

        // Transliteration matching
        val arToEnDict = mapOf(
            "دوسري" to listOf("dosari", "dossary", "dossari"),
            "باسط" to listOf("basit", "basset", "bassit"),
            "عفاسي" to listOf("afasy", "afasi", "alafasy"),
            "معيقلي" to listOf("muaiqly", "moaiqli", "muayqly", "maher"),
            "حصري" to listOf("hussary", "hosary", "husary"),
            "منشاوي" to listOf("minshawi", "menshawi"),
            "سديس" to listOf("sudais", "sudaes", "sudays"),
            "شريم" to listOf("shuraim", "shuraym", "churaim"),
            "قران" to listOf("quran", "qoran", "koran"),
            "سورة" to listOf("surah", "surat", "sura")
        )

        for ((ar, enList) in arToEnDict) {
            if (q.contains(ar)) {
                for (en in enList) {
                    if (t.contains(en)) return true
                }
            }
        }
        return false
    }
}
