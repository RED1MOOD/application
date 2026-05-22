package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioFile
import com.example.util.Language
import com.example.util.LocalizationManager
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranAudioViewModel

data class OnlineQari(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val serverUrl: String, // e.g. https://server8.mp3quran.net/afs
    val gradientColors: List<Color>,
    val locationAr: String,
    val locationEn: String,
    val categoryAr: String,
    val categoryEn: String
)

val onlineQaris = listOf(
    // 1. أئمة وقراء الحرمين الشريفين
    OnlineQari(
        id = "yasser",
        arabicName = "ياسر الدوسري",
        englishName = "Yasser Al-Dousari",
        serverUrl = "https://server11.mp3quran.net/yasser",
        gradientColors = listOf(Color(0xFFB45309), Color(0xFFF59E0B)),
        locationAr = "المسجد الحرام",
        locationEn = "Al-Masjid Al-Haram",
        categoryAr = "أئمة وقراء الحرمين الشريفين",
        categoryEn = "Imams of the Two Holy Mosques"
    ),
    OnlineQari(
        id = "maher",
        arabicName = "ماهر المعيقلي",
        englishName = "Maher Al-Muaiqly",
        serverUrl = "https://server12.mp3quran.net/maher",
        gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)),
        locationAr = "المسجد الحرام",
        locationEn = "Al-Masjid Al-Haram",
        categoryAr = "أئمة وقراء الحرمين الشريفين",
        categoryEn = "Imams of the Two Holy Mosques"
    ),
    OnlineQari(
        id = "sds",
        arabicName = "عبد الرحمن السديس",
        englishName = "Abdul Rahman Al-Sudais",
        serverUrl = "https://server11.mp3quran.net/sds",
        gradientColors = listOf(Color(0xFF047857), Color(0xFF10B981)),
        locationAr = "المسجد الحرام",
        locationEn = "Al-Masjid Al-Haram",
        categoryAr = "أئمة وقراء الحرمين الشريفين",
        categoryEn = "Imams of the Two Holy Mosques"
    ),
    OnlineQari(
        id = "shur",
        arabicName = "سعود الشريم",
        englishName = "Saud Al-Shuraim",
        serverUrl = "https://server7.mp3quran.net/shur",
        gradientColors = listOf(Color(0xFF3730A3), Color(0xFF8B5CF6)),
        locationAr = "المسجد الحرام (سابقاً)",
        locationEn = "Al-Masjid Al-Haram",
        categoryAr = "أئمة وقراء الحرمين الشريفين",
        categoryEn = "Imams of the Two Holy Mosques"
    ),
    OnlineQari(
        id = "ayyoub",
        arabicName = "محمد أيوب",
        englishName = "Muhammad Ayyoub",
        serverUrl = "https://server8.mp3quran.net/ayyoub",
        gradientColors = listOf(Color(0xFF1F2937), Color(0xFF6B7280)),
        locationAr = "المسجد النبوي الشريف",
        locationEn = "Al-Masjid Al-Nabawi",
        categoryAr = "أئمة وقراء الحرمين الشريفين",
        categoryEn = "Imams of the Two Holy Mosques"
    ),
    OnlineQari(
        id = "huza",
        arabicName = "علي الحذيفي",
        englishName = "Ali Al-Huthaify",
        serverUrl = "https://server9.mp3quran.net/huza",
        gradientColors = listOf(Color(0xFF334155), Color(0xFF64748B)),
        locationAr = "المسجد النبوي الشريف",
        locationEn = "Al-Masjid Al-Nabawi",
        categoryAr = "أئمة وقراء الحرمين الشريفين",
        categoryEn = "Imams of the Two Holy Mosques"
    ),

    // 2. شيوخ مدرسة التلاوة بمصر
    OnlineQari(
        id = "husr",
        arabicName = "محمود خليل الحصري",
        englishName = "Mahmoud Khalil Al-Husary",
        serverUrl = "https://server13.mp3quran.net/husr",
        gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)),
        locationAr = "جمهورية مصر العربية",
        locationEn = "Egypt",
        categoryAr = "شيوخ مدرسة التلاوة بمصر",
        categoryEn = "Egyptian Reciters School"
    ),
    OnlineQari(
        id = "basit_m",
        arabicName = "عبد الباسط عبد الصمد",
        englishName = "Abdul Basit Abdul Samad",
        serverUrl = "https://server7.mp3quran.net/basit",
        gradientColors = listOf(Color(0xFF701A75), Color(0xFFD946EF)),
        locationAr = "جمهورية مصر العربية",
        locationEn = "Egypt",
        categoryAr = "شيوخ مدرسة التلاوة بمصر",
        categoryEn = "Egyptian Reciters School"
    ),
    OnlineQari(
        id = "minsh_m",
        arabicName = "محمد صديق المنشاوي",
        englishName = "Mohamed Siddiq Al-Minshawi",
        serverUrl = "https://server10.mp3quran.net/minsh",
        gradientColors = listOf(Color(0xFF9F1239), Color(0xFFF43F5E)),
        locationAr = "جمهورية مصر العربية",
        locationEn = "Egypt",
        categoryAr = "شيوخ مدرسة التلاوة بمصر",
        categoryEn = "Egyptian Reciters School"
    ),

    // 3. أشهر الأصوات العذبة بالوطن الإسلامي
    OnlineQari(
        id = "afs",
        arabicName = "مشاري بن راشد العفاسي",
        englishName = "Mishary Rashid Alafasy",
        serverUrl = "https://server8.mp3quran.net/afs",
        gradientColors = listOf(Color(0xFF0F766E), Color(0xFF14B8A6)),
        locationAr = "دولة الكويت",
        locationEn = "Kuwait",
        categoryAr = "أشهر الأصوات العذبة بالوطن الإسلامي",
        categoryEn = "Beautiful Voices of the Islamic World"
    ),
    OnlineQari(
        id = "frs_a",
        arabicName = "فارس عباد",
        englishName = "Fares Abbad",
        serverUrl = "https://server8.mp3quran.net/frs_a",
        gradientColors = listOf(Color(0xFF0369A1), Color(0xFF0EA5E9)),
        locationAr = "الجمهورية اليمنية",
        locationEn = "Yemen",
        categoryAr = "أشهر الأصوات العذبة بالوطن الإسلامي",
        categoryEn = "Beautiful Voices of the Islamic World"
    ),
    OnlineQari(
        id = "s_gmd",
        arabicName = "سعد الغامدي",
        englishName = "Saad Al-Ghamdi",
        serverUrl = "https://server7.mp3quran.net/s_gmd",
        gradientColors = listOf(Color(0xFF7C2D12), Color(0xFFEA580C)),
        locationAr = "المملكة العربية السعودية",
        locationEn = "Saudi Arabia",
        categoryAr = "أشهر الأصوات العذبة بالوطن الإسلامي",
        categoryEn = "Beautiful Voices of the Islamic World"
    ),
    OnlineQari(
        id = "hazza",
        arabicName = "هزاع البلوشي",
        englishName = "Hazza Al-Balushi",
        serverUrl = "https://server11.mp3quran.net/hazza",
        gradientColors = listOf(Color(0xFF4D7C0F), Color(0xFF84CC16)),
        locationAr = "سلطنة عمان",
        locationEn = "Oman",
        categoryAr = "أشهر الأصوات العذبة بالوطن الإسلامي",
        categoryEn = "Beautiful Voices of the Islamic World"
    ),
    OnlineQari(
        id = "qtm",
        arabicName = "ناصر القطامي",
        englishName = "Nasser Al-Qatami",
        serverUrl = "https://server6.mp3quran.net/qtm",
        gradientColors = listOf(Color(0xFF4A0E4E), Color(0xFF9C27B0)),
        locationAr = "المملكة العربية السعودية",
        locationEn = "Saudi Arabia",
        categoryAr = "أشهر الأصوات العذبة بالوطن الإسلامي",
        categoryEn = "Beautiful Voices of the Islamic World"
    ),
    OnlineQari(
        id = "abkr",
        arabicName = "إدريس أبكر",
        englishName = "Idris Abkar",
        serverUrl = "https://server6.mp3quran.net/abkr",
        gradientColors = listOf(Color(0xFF134E5E), Color(0xFF71B280)),
        locationAr = "المملكة العربية السعودية",
        locationEn = "Saudi Arabia",
        categoryAr = "أشهر الأصوات العذبة بالوطن الإسلامي",
        categoryEn = "Beautiful Voices of the Islamic World"
    )
)

val surahsEnglishList = listOf(
    "Al-Fatihah", "Al-Baqarah", "Ali 'Imran", "An-Nisa'", "Al-Ma'idah", "Al-An'am", "Al-A'raf", "Al-Anfal", "At-Tawbah", 
    "Yunus", "Hud", "Yusuf", "Ar-Ra'd", "Ibrahim", "Al-Hijr", "An-Nahl", "Al-Isra'", "Al-Kahf", "Maryam", "Ta-Ha", 
    "Al-Anbiya'", "Al-Hajj", "Al-Mu'minun", "An-Nur", "Al-Furqan", "Ash-Shu'ara'", "An-Naml", "Al-Qasas", "Al-Ankabut", 
    "Ar-Rum", "Luqman", "As-Sajdah", "Al-Ahzab", "Saba'", "Fatir", "Ya-Sin", "As-Saffat", "Sad", "Az-Zumar", 
    "Ghafir", "Fussilat", "Ash-Shura", "Az-Zukhruf", "Ad-Dukhan", "Al-Jathiyah", "Al-Ahqaf", "Muhammad", "Al-Fath", "Al-Hujurat", 
    "Qaf", "Adh-Dhariyat", "At-Tur", "An-Najm", "Al-Qamar", "Ar-Rahman", "Al-Waqi'ah", "Al-Hadid", "Al-Mujadilah", "Al-Hashr", 
    "Al-Mumtahanah", "As-Saff", "Al-Jumu'ah", "Al-Munafiqun", "At-Taghabun", "At-Talaq", "At-Tahrim", "Al-Mulk", "Al-Qalam", 
    "Al-Haqqah", "Al-Ma'arij", "Nuh", "Al-Jinn", "Al-Muzzammil", "Al-Muddaththir", "Al-Qiyamah", "Al-Insan", "Al-Mursalat", 
    "An-Naba'", "An-Nazi'at", "'Abasa", "At-Takwir", "Al-Infitar", "Al-Mutaffifin", "Al-Inshiqaq", "Al-Buruj", "At-Tariq", 
    "Al-A'la", "Al-Ghashiyah", "Al-Fajr", "Al-Balad", "Ash-Shams", "Al-Layl", "Ad-Duha", "Ash-Sharh", "At-Tin", 
    "Al-Alaq", "Al-Qadr", "Al-Bayyinah", "Az-Zalzalah", "Al-Adiyat", "Al-Qari'ah", "At-Takathur", "Al-Asr", "Al-Humazah", 
    "Al-Fil", "Quraysh", "Al-Ma'un", "Al-Kauthar", "Al-Kafirun", "An-Nasr", "Al-Masad", "Ikhlas", "Al-Falaq", "An-Nas"
)

val surahsArabicList = listOf(
    "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", 
    "يونس", "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه", 
    "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", 
    "الروم", "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", 
    "غافر", "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", 
    "ق", "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", 
    "الممتحنة", "الصف", "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", 
    "الحاقة", "المعارج", "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", 
    "النبأ", "النازعات", "عبس", "التكوير", "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", 
    "الأعلى", "الغاشية", "الفجر", "البلد", "الشمس", "الليل", "الضحى", "الشرح", "التين", 
    "العلق", "القدر", "البينة", "الزلزلة", "العاديات", "القارعة", "التكاثر", "العصر", "الهمزة", 
    "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر", "المسد", "الإخلاص", "الفلق", "الناس"
)

@Composable
fun OnlineSectionView(viewModel: QuranAudioViewModel) {
    val langState by LocalizationManager.currentLanguage.collectAsState()
    val context = LocalContext.current

    var selectedQari by remember { mutableStateOf<OnlineQari?>(null) }
    var onlineSearchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        if (selectedQari == null) {
            // Online Banner Header
            Text(
                text = LocalizationManager.get("online_header"),
                color = PureWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = LocalizationManager.get("online_subtitle"),
                color = LightGrayText,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Filter Row
            OutlinedTextField(
                value = onlineSearchQuery,
                onValueChange = { onlineSearchQuery = it },
                placeholder = { Text(LocalizationManager.get("search_hint"), color = LightGrayText, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = QuranPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = QuranPrimary,
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite,
                    focusedContainerColor = Color(0x0CFFFFFF),
                    unfocusedContainerColor = Color(0x0CFFFFFF)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (onlineSearchQuery.isNotEmpty()) {
                val filteredQaris = remember(onlineSearchQuery, langState) {
                    onlineQaris.filter {
                        it.englishName.contains(onlineSearchQuery, ignoreCase = true) ||
                        it.arabicName.contains(onlineSearchQuery, ignoreCase = true)
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredQaris) { qari ->
                        QariGridCard(qari, langState) {
                            selectedQari = qari
                        }
                    }
                }
            } else {
                val categories = remember(langState) {
                    if (langState == Language.AR) {
                        listOf(
                            "أئمة وقراء الحرمين الشريفين" to onlineQaris.filter { it.categoryAr == "أئمة وقراء الحرمين الشريفين" },
                            "شيوخ مدرسة التلاوة بمصر" to onlineQaris.filter { it.categoryAr == "شيوخ مدرسة التلاوة بمصر" },
                            "أشهر الأصوات العذبة بالوطن الإسلامي" to onlineQaris.filter { it.categoryAr == "أشهر الأصوات العذبة بالوطن الإسلامي" }
                        )
                    } else {
                        listOf(
                            "Imams of the Two Holy Mosques" to onlineQaris.filter { it.categoryEn == "Imams of the Two Holy Mosques" },
                            "Egyptian Reciters School" to onlineQaris.filter { it.categoryEn == "Egyptian Reciters School" },
                            "Beautiful Voices of the Islamic World" to onlineQaris.filter { it.categoryEn == "Beautiful Voices of the Islamic World" }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(categories) { (categoryName, qarisList) ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp, 16.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(QuranPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = categoryName,
                                    color = PureWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                qarisList.forEach { qari ->
                                    QariCarouselCard(qari, langState) {
                                        selectedQari = qari
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // We have selected a Qari, display their beautiful Surah list!
            val qari = selectedQari!!

            // Back button and Qari header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedQari = null },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x1AFFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PureWhite
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = if (langState == Language.AR) qari.arabicName else qari.englishName,
                        color = PureWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "114 ${LocalizationManager.get("by_surah")}",
                        color = QuranPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick search for surahs
            OutlinedTextField(
                value = onlineSearchQuery,
                onValueChange = { onlineSearchQuery = it },
                placeholder = { Text(LocalizationManager.get("search_hint"), color = LightGrayText, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = QuranPrimary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = QuranPrimary,
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite,
                    focusedContainerColor = Color(0x0CFFFFFF),
                    unfocusedContainerColor = Color(0x0CFFFFFF)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Surah List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val indexes = (1..114).toList()
                val filteredIndexes = indexes.filter { idx ->
                    val arName = surahsArabicList[idx - 1]
                    val enName = surahsEnglishList[idx - 1]
                    arName.contains(onlineSearchQuery, ignoreCase = true) ||
                    enName.contains(onlineSearchQuery, ignoreCase = true) ||
                    idx.toString() == onlineSearchQuery
                }

                items(filteredIndexes) { idx ->
                    val surahNumberFormatted = String.format("%03d", idx)
                    // Server path: e.g., https://server8.mp3quran.net/afs/001.mp3
                    val streamUrl = "${qari.serverUrl}/$surahNumberFormatted.mp3"
                    val surahArabic = surahsArabicList[idx - 1]
                    val surahEnglish = surahsEnglishList[idx - 1]
                    val displayTitle = if (langState == Language.AR) "سورة $surahArabic" else "Surah $surahEnglish"
                    val artistDisplay = if (langState == Language.AR) qari.arabicName else qari.englishName

                    val onlineTrack = AudioFile(
                        filePath = streamUrl,
                        title = displayTitle,
                        artist = artistDisplay,
                        album = "Cloud Server Broadcast",
                        durationMs = 0L, // Streaming resolves duration over internet
                        sizeBytes = 0L,
                        category = "QURAN",
                        qariName = qari.englishName,
                        surahName = displayTitle,
                        isComplete = true,
                        qualityKbps = 192
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(LightGlassBg)
                            .border(1.dp, BorderWhiteAlpha, RoundedCornerShape(16.dp))
                            .clickable {
                                Toast.makeText(context, LocalizationManager.get("connecting"), Toast.LENGTH_SHORT).show()
                                // Play online track setting current track inside VM
                                viewModel.playTrack(onlineTrack, listOf(onlineTrack))
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Badge index indicator
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(QuranPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = idx.toString(),
                                color = QuranPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayTitle,
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (langState == Language.AR) qari.locationAr else qari.locationEn,
                                color = LightGrayText,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                Toast.makeText(context, LocalizationManager.get("connecting"), Toast.LENGTH_SHORT).show()
                                viewModel.playTrack(onlineTrack, listOf(onlineTrack))
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(QuranPrimary.copy(alpha = 0.1f))
                                .size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = QuranPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QariCarouselCard(
    qari: OnlineQari,
    langState: Language,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = LightGlassBg),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, BorderWhiteAlpha)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Artistic Initial Letter Badge
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = qari.gradientColors
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val initials = if (langState == Language.AR) {
                    qari.arabicName.split(" ").firstOrNull()?.take(2) ?: "ق"
                } else {
                    qari.englishName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2)
                }
                Text(
                    text = initials,
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (langState == Language.AR) qari.arabicName else qari.englishName,
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = QuranSecondary,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (langState == Language.AR) qari.locationAr else qari.locationEn,
                    color = LightGrayText,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action label
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(QuranPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = LocalizationManager.get("select_reciter"),
                    color = QuranPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QariGridCard(
    qari: OnlineQari,
    langState: Language,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = LightGlassBg),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, BorderWhiteAlpha)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Artistic Initial Letter Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = qari.gradientColors
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val initials = if (langState == Language.AR) {
                    qari.arabicName.split(" ").firstOrNull()?.take(2) ?: "ق"
                } else {
                    qari.englishName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2)
                }
                Text(
                    text = initials,
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (langState == Language.AR) qari.arabicName else qari.englishName,
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = QuranSecondary,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (langState == Language.AR) qari.locationAr else qari.locationEn,
                    color = LightGrayText,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action label
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(QuranPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = LocalizationManager.get("select_reciter"),
                    color = QuranPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
