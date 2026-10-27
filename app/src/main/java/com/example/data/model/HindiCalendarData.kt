package com.example.data.model

object HindiCalendarData {

    val hindiMonths = listOf(
        HindiMonth("Chaitra", "चैत्र", "Spring (वसन्त)", "March - April"),
        HindiMonth("Vaishakha", "वैशाख", "Summer (ग्रीष्म)", "April - May"),
        HindiMonth("Jyeshtha", "ज्येष्ठ", "Summer (ग्रीष्म)", "May - June"),
        HindiMonth("Ashadha", "आषाढ़", "Monsoon (वर्षा)", "June - July"),
        HindiMonth("Shravana", "श्रावण", "Monsoon (वर्षा)", "July - August"),
        HindiMonth("Bhadrapada", "भाद्रपद", "Monsoon (वर्षा)", "August - September"),
        HindiMonth("Ashwin", "आश्विन", "Autumn (शरद)", "September - October"),
        HindiMonth("Kartika", "कार्तिक", "Autumn (शरद)", "October - November"),
        HindiMonth("Margashirsha", "मार्गशीर्ष", "Pre-Winter (हेमन्त)", "November - December"),
        HindiMonth("Pausha", "पौष", "Winter (शिशिर)", "December - January"),
        HindiMonth("Magha", "माघ", "Winter (शिशिर)", "January - February"),
        HindiMonth("Phalguna", "फाल्गुन", "Spring (वसन्त)", "February - March")
    )

    val defaultHolidays = listOf(
        HolidayItem(
            id = "h_1",
            englishName = "Makar Sankranti / Pongal",
            hindiName = "मकर संक्रांति / पोंगल",
            dateString = "14 Jan 2027",
            hindiMonth = "Magha",
            tithi = "Shukla Pratipada",
            region = "National",
            description = "Harvest festival marking Uttarayan sun transit; kite flying in Gujarat and Pongal in South India."
        ),
        HolidayItem(
            id = "h_2",
            englishName = "Maha Shivratri",
            hindiName = "महाशिवरात्रि",
            dateString = "06 Mar 2027",
            hindiMonth = "Phalguna",
            tithi = "Krishna Chaturdashi",
            region = "National",
            description = "Great Night of Shiva, fasting and midnight vigil across sacred Jyotirlingas."
        ),
        HolidayItem(
            id = "h_3",
            englishName = "Holi (Dhulandi)",
            hindiName = "होली (धुलेंडी)",
            dateString = "23 Mar 2027",
            hindiMonth = "Phalguna",
            tithi = "Purnima",
            region = "National",
            description = "Festival of colors, victory of good over evil, celebrated with gulal and sweets."
        ),
        HolidayItem(
            id = "h_4",
            englishName = "Chaitra Navratri / Gudi Padwa",
            hindiName = "चैत्र नवरात्रि / गुड़ी पड़वा",
            dateString = "08 Apr 2027",
            hindiMonth = "Chaitra",
            tithi = "Shukla Pratipada",
            region = "North & West",
            description = "Hindu New Year (Vikram Samvat 2084 begins), 9 sacred days of Goddess Durga."
        ),
        HolidayItem(
            id = "h_5",
            englishName = "Ram Navami",
            hindiName = "राम नवमी",
            dateString = "16 Apr 2027",
            hindiMonth = "Chaitra",
            tithi = "Shukla Navami",
            region = "National",
            description = "Celebration of the birth of Lord Rama with processions and hymns in Ayodhya."
        ),
        HolidayItem(
            id = "h_6",
            englishName = "Raksha Bandhan",
            hindiName = "रक्षाबंधन",
            dateString = "28 Aug 2027",
            hindiMonth = "Shravana",
            tithi = "Purnima",
            region = "North & Central",
            description = "Celebration of sibling bond; sisters tie sacred rakhi on brothers' wrists."
        ),
        HolidayItem(
            id = "h_7",
            englishName = "Krishna Janmashtami",
            hindiName = "कृष्ण जन्माष्टमी",
            dateString = "04 Sep 2027",
            hindiMonth = "Bhadrapada",
            tithi = "Krishna Ashtami",
            region = "National",
            description = "Birth of Lord Krishna celebrated with midnight prayers and Dahi Handi."
        ),
        HolidayItem(
            id = "h_8",
            englishName = "Ganesh Chaturthi",
            hindiName = "गणेश चतुर्थी",
            dateString = "14 Sep 2027",
            hindiMonth = "Bhadrapada",
            tithi = "Shukla Chaturthi",
            region = "West & South",
            description = "10-day grand festival welcoming Lord Ganesha in Maharashtra, Goa, and Karnataka."
        ),
        HolidayItem(
            id = "h_9",
            englishName = "Durga Puja & Dussehra (Vijayadashami)",
            hindiName = "दुर्गा पूजा एवं विजयादशमी",
            dateString = "19 Oct 2027",
            hindiMonth = "Ashwin",
            tithi = "Shukla Dashami",
            region = "East & National",
            description = "Victory of Durga over Mahishasura in Bengal, Ramlila effigy burning across North India."
        ),
        HolidayItem(
            id = "h_10",
            englishName = "Karwa Chauth",
            hindiName = "करवा चौथ",
            dateString = "28 Oct 2027",
            hindiMonth = "Kartika",
            tithi = "Krishna Chaturthi",
            region = "North",
            description = "Fasting for spouse longevity from sunrise to moonrise across Northern India."
        ),
        HolidayItem(
            id = "h_11",
            englishName = "Diwali (Deepavali & Lakshmi Puja)",
            hindiName = "दीपावली (लक्ष्मी पूजन)",
            dateString = "08 Nov 2027",
            hindiMonth = "Kartika",
            tithi = "Amavasya",
            region = "National",
            description = "Festival of lights, diyas, fireworks, prosperity, welcoming Goddess Lakshmi."
        ),
        HolidayItem(
            id = "h_12",
            englishName = "Chhath Puja",
            hindiName = "छठ पूजा (महापर्व)",
            dateString = "14 Nov 2027",
            hindiMonth = "Kartika",
            tithi = "Shukla Shashthi",
            region = "East & North",
            description = "Ancient 4-day Vedic festival offering Arghya to the setting and rising Sun in Bihar & UP."
        ),
        HolidayItem(
            id = "h_13",
            englishName = "Guru Nanak Jayanti (Gurpurab)",
            hindiName = "गुरु नानक जयंती",
            dateString = "23 Nov 2027",
            hindiMonth = "Kartika",
            tithi = "Purnima",
            region = "North & National",
            description = "Prakash Utsav honoring the founder of Sikhism with Prabhat Pheris and Langar."
        )
    )

    fun calculatePanchangToday(): Map<String, String> {
        val samvatYear = 2083
        val shakaYear = 1948
        return mapOf(
            "samvat" to "विक्रम संवत $samvatYear (कालयुक्त)",
            "shaka" to "शक संवत $shakaYear",
            "maas" to "कार्तिक मास (Kartika)",
            "paksha" to "शुक्ल पक्ष (Shukla Paksha)",
            "tithi" to "द्वादशी (Dwadashi)",
            "nakshatra" to "रोहिणी (Rohini)",
            "yoga" to "वृद्धि (Vriddhi)",
            "karana" to "बव (Bava)",
            "rahuKaal" to "10:30 AM - 12:00 PM",
            "abhijitMuhurat" to "11:45 AM - 12:35 PM"
        )
    }
}
